package lelysi.scalashop

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Route}
import model.{Email, ItemToCart, ItemUuid, ShopItem, User, UserLogin}
import akka.http.scaladsl.server.Directives._
import lelysi.scalashop.service.{CartService, UserService, WarehouseService}
import akka.pattern.ask
import akka.util.Timeout
import lelysi.scalashop.service.CartService.{CartServiceResponse, ItemAddedToCart, ItemWasNotFound}
import lelysi.scalashop.service.UserService.{AuthUser, EmailAlreadyUsed, IncorrectPassword, RegisterUser, UserAuthenticationResponse, UserFound, UserRegistered, UserRegistrationResponse, UserSearchResponse, UserUnknown}
import lelysi.scalashop.service.WarehouseService.{AddItem, ItemAdded, WarehouseServiceAddItemResponse}
import scala.concurrent.Future
import scala.util.{Failure, Success}

trait ShopRoute {
  this: JsonSupport =>

  implicit def requestTimeout: Timeout

  def userRegistration: Route =
    path("registration") {
      post {
        entity(as[User]) { user =>
          onSuccess(registerUser(user)) {
            case UserRegistered => complete(s"New user with email ${user.email.toString} was registered")
            case EmailAlreadyUsed => complete(StatusCodes.BadRequest, s"user already exists!")
            case _ => complete(StatusCodes.BadRequest, s"registration failed")
          }
        }
      }
    }

  def login: Route =
    path("login") {
      post {
        entity(as[UserLogin]) { userLogin =>
          onSuccess(authenticate(userLogin)) {
            case UserUnknown | IncorrectPassword => reject(AuthorizationFailedRejection)
            case UserFound(user) => complete(StatusCodes.OK, JwtAuthentication.getToken(user.email.toString))
          }
        }
      }
    }

  def addShopItem(): Route =
    path("add-shop-item") {
      post {
        entity(as[ShopItem]) { shopItem =>
          onSuccess(addItem(shopItem)) {
            case ItemAdded => complete(s"New item with uuid ${shopItem.uuid} was added")
            case _ => complete(StatusCodes.BadRequest, s"failed")
          }
        }
      }
    }

  def addItemToCartRoute(): Route =
    path("add-item-to-cart") {
      post {
        entity(as[ItemUuid]) { itemUuid =>
          headerValueByName("X-Api-Key") { token =>
            JwtAuthentication.getDecodedClaim(token) match {
              case Success(claimData) => onSuccess(addItemToCart(ItemToCart(Email(claimData.content), itemUuid.uuid))) {
                case ItemAddedToCart => complete(s"item added")
                case ItemWasNotFound => complete(StatusCodes.BadRequest, s"item was not found")
              }
              case Failure(_) => complete(StatusCodes.Unauthorized, s"invalid key")
            }
          }
        }
      }
    }

  val routes: Route = userRegistration ~ login ~ addShopItem() ~ addItemToCartRoute()

  val userService: ActorRef
  val warehouseService: ActorRef
  val cartService: ActorRef

  def registerUser(user: User): Future[UserRegistrationResponse] =
    userService.ask(RegisterUser(user)).mapTo[UserRegistrationResponse]

  def authenticate(userLogin: UserLogin): Future[UserAuthenticationResponse] =
    userService.ask(AuthUser(userLogin)).mapTo[UserAuthenticationResponse]

  def addItem(shopItem: ShopItem): Future[WarehouseServiceAddItemResponse] =
    warehouseService.ask(AddItem(shopItem)).mapTo[WarehouseServiceAddItemResponse]

  def addItemToCart(itemToCart: ItemToCart): Future[CartServiceResponse] =
    cartService.ask(itemToCart).mapTo[CartServiceResponse]
}

class ShopApi(system: ActorSystem, timeout: Timeout) extends ShopRoute with JsonSupport {
  implicit val requestTimeout: Timeout = timeout
  lazy val userService: ActorRef = system.actorOf(Props[UserService])
  lazy val warehouseService: ActorRef = system.actorOf(Props[WarehouseService])
  lazy val cartService: ActorRef = system.actorOf(CartService.props(warehouseService))
}
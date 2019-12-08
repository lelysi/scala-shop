package lelysi.scalashop

import com.typesafe.config.Config
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Route}
import model.{AddItemToWarehouse, Email, ItemUuid, ShopItem, User, UserLogin}
import akka.http.scaladsl.server.Directives._
import lelysi.scalashop.service.{CartService, CheckoutService, UserService, WarehouseService}
import akka.pattern.ask
import akka.util.Timeout
import lelysi.scalashop.service.CartService.{ItemAddedToCart, ItemAddingResponse, ItemToCart, ItemWasNotFound}
import lelysi.scalashop.service.CheckoutService.{CheckoutFail, CheckoutRequest, CheckoutServiceResponse, CheckoutSuccess}
import lelysi.scalashop.service.UserService.{AuthUser, EmailAlreadyUsed, IncorrectPassword, RegisterUser, UserAuthenticationResponse, UserFound, UserRegistered, UserRegistrationResponse, UserSearchResponse, UserUnknown}
import lelysi.scalashop.service.WarehouseService.{AddItem, ItemAdded, WarehouseServiceAddItemResponse}
import lelysi.scalashop.service.paymentgate.{FakePaymentGate, Order}

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait ShopRoute {
  this: JsonSupport =>

  implicit val timeout: Timeout

  def userRegistration(): Route =
    path("registration") {
      post {
        entity(as[User]) { user =>
          onSuccess(registerUser(user)) {
            case UserRegistered => complete(jwtAuthenticator.getToken(user.email.toString))
            case EmailAlreadyUsed => complete(StatusCodes.BadRequest, s"user already exists!")
            case _ => complete(StatusCodes.InternalServerError, s"registration failed")
          }
        }
      }
    }

  def login(): Route =
    path("login") {
      post {
        entity(as[UserLogin]) { userLogin =>
          onSuccess(authenticate(userLogin)) {
            case UserUnknown | IncorrectPassword => reject(AuthorizationFailedRejection)
            case UserFound(user) => complete(jwtAuthenticator.getToken(user.email.toString))
          }
        }
      }
    }

  def addShopItem(): Route =
    path("add-shop-item") {
      post {
        entity(as[AddItemToWarehouse]) { itemToWarehouse =>
          onSuccess(addItem(itemToWarehouse)) {
            case ItemAdded => complete(s"New item with uuid ${itemToWarehouse.shopItem.uuid} was added")
            case _ => complete(StatusCodes.InternalServerError, s"failed")
          }
        }
      }
    }

  def addItemToCartRoute(): Route =
    path("add-item-to-cart") {
      post {
        entity(as[ItemUuid]) { itemUuid =>
          headerValueByName("X-Api-Key") { token =>
            jwtAuthenticator.getDecodedClaim(token) match {
              case Success(claimData) =>
                onSuccess(addItemToCart(ItemToCart(Email(claimData.content), itemUuid.uuid))) {
                case ItemAddedToCart => complete(s"item added")
                case ItemWasNotFound => complete(StatusCodes.BadRequest, s"item was not found")
              }
              case Failure(_) => complete(StatusCodes.Unauthorized, s"invalid key")
            }
          }
        }
      }
    }

  def checkoutRoute(): Route =
    path("checkout") {
      post {
        headerValueByName("X-Api-Key") { token =>
          jwtAuthenticator.getDecodedClaim(token) match {
            case Success(claimData) => onSuccess(checkout(CheckoutRequest(Email(claimData.content)))) {
              case CheckoutSuccess(Order(uuid, _, _)) => complete(s"success, order nr $uuid")
              case CheckoutFail => complete(StatusCodes.BadRequest, s"error")
            }
            case Failure(_) => complete(StatusCodes.Unauthorized, s"invalid key")
          }
        }
      }
    }

  val routes: Route =
    userRegistration() ~ login() ~ addShopItem() ~ addItemToCartRoute() ~ checkoutRoute()

  val userService: ActorRef
  val warehouseService: ActorRef
  val cartService: ActorRef
  val checkoutService: ActorRef
  val jwtAuthenticator: JwtAuthentication

  def registerUser(user: User): Future[UserRegistrationResponse] =
    userService.ask(RegisterUser(user)).mapTo[UserRegistrationResponse]

  def authenticate(userLogin: UserLogin): Future[UserAuthenticationResponse] =
    userService.ask(AuthUser(userLogin)).mapTo[UserAuthenticationResponse]

  def addItem(itemToWarehouse: AddItemToWarehouse): Future[WarehouseServiceAddItemResponse] =
    warehouseService.ask(AddItem(itemToWarehouse)).mapTo[WarehouseServiceAddItemResponse]

  def addItemToCart(itemToCart: ItemToCart): Future[ItemAddingResponse] =
    cartService.ask(itemToCart).mapTo[ItemAddingResponse]

  def checkout(checkoutRequest: CheckoutRequest): Future[CheckoutServiceResponse] =
    checkoutService.ask(checkoutRequest).mapTo[CheckoutServiceResponse]
}

class ShopApi(system: ActorSystem)(implicit val timeout: Timeout, implicit val config: Config) extends ShopRoute
  with JsonSupport {

  lazy val userService: ActorRef = system.actorOf(Props[UserService])
  lazy val warehouseService: ActorRef = system.actorOf(Props[WarehouseService])
  lazy val cartService: ActorRef = system.actorOf(CartService.props(warehouseService))
  lazy val paymentGate: ActorRef = system.actorOf(Props[FakePaymentGate])
  lazy val checkoutService: ActorRef = system.actorOf(
    CheckoutService.props(userService, cartService, warehouseService, paymentGate)
  )
  lazy val jwtAuthenticator: JwtAuthentication = new JwtAuthentication(config)
}
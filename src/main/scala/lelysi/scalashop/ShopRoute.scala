package lelysi.scalashop

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Route}
import model.{ShopItem, User, UserLogin}
import akka.http.scaladsl.server.Directives._
import lelysi.scalashop.service.{UserService, WarehouseService}
import akka.pattern.ask
import akka.util.Timeout
import lelysi.scalashop.service.UserService.{AuthUser, EmailAlreadyUsed, IncorrectPassword, RegisterUser, UserAuthenticationResponse, UserFound, UserRegistered, UserRegistrationResponse, UserUnknown}
import lelysi.scalashop.service.WarehouseService.{AddItem, ItemAdded, WarehouseServiceResponse}

import scala.concurrent.Future

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
            case UserFound => complete(StatusCodes.OK, JwtAuthentication.generateToken(userLogin.email.toString))
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

  val routes: Route = userRegistration ~ login ~ addShopItem()

  val userService: ActorRef
  val warehouseService: ActorRef

  def registerUser(user: User): Future[UserRegistrationResponse] =
    userService.ask(RegisterUser(user)).mapTo[UserRegistrationResponse]

  def authenticate(userLogin: UserLogin): Future[UserAuthenticationResponse] =
    userService.ask(AuthUser(userLogin)).mapTo[UserAuthenticationResponse]

  def addItem(shopItem: ShopItem): Future[WarehouseServiceResponse] =
    warehouseService.ask(AddItem(shopItem)).mapTo[WarehouseServiceResponse]
}

class ShopApi(system: ActorSystem, timeout: Timeout) extends ShopRoute with JsonSupport {
  implicit val requestTimeout: Timeout = timeout
  lazy val userService: ActorRef = system.actorOf(Props[UserService])
  lazy val warehouseService: ActorRef = system.actorOf(Props[WarehouseService])
}
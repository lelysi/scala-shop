package lelysi.scalashop

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Directive1, Route}
import model.{ShopItem, User}
import spray.json.{DefaultJsonProtocol, JsArray, JsNumber, JsString, JsValue, RootJsonFormat}
import akka.http.scaladsl.server.Directives._
import lelysi.scalashop.service.{UserService, WarehouseService}
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.pattern.ask
import akka.util.Timeout
import com.emarsys.jwt.akka.http.{JwtAuthentication, JwtConfig}
import lelysi.scalashop.service.UserService.{AuthUser, EmailAlreadyUsed, RegisterUser, UserAuthenticationResponse, UserFound, UserRegistered, UserRegistrationResponse, UserServiceResponse, UserUnknown}
import lelysi.scalashop.service.WarehouseService.{AddItem, ItemAdded, WarehouseServiceResponse}
import spray.json._
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat2(User)

  implicit val shopItemFormat: RootJsonFormat[ShopItem] = new RootJsonFormat[ShopItem] {
    override def read(json: JsValue): ShopItem = json.asJsObject().getFields("price", "description") match {
      case Seq(JsNumber(price), JsString(description)) => new ShopItem(price.toDouble, description.toString)
      case _ => throw DeserializationException("ShopItem expected" + json)
    }
    override def write(obj: ShopItem): JsValue = JsArray(JsNumber(obj.price), JsString(obj.description))
  }
}

trait ShopRoute {
  this: JsonSupport =>

  implicit def requestTimeout: Timeout

  val userRegistration: Route =
    path("registration") {
      post {
        entity(as[User]) { user =>
          onSuccess(registerUser(user)) {
            case UserRegistered => complete(s"New user with email ${user.email} was registered")
            case EmailAlreadyUsed => complete(BadRequest, s"user already exists!")
            case _ => complete(BadRequest, s"registration failed")
          }
        }
      }
    }

  val login: Route =
    path("login") {
      post {
        entity(as[User]) { user =>
          onSuccess(authenticate(user)) {
            case UserUnknown => reject(AuthorizationFailedRejection)
            case UserFound => complete(StatusCodes.OK, JwtAuthenticationObj.generateToken(user.email))
          }
        }
      }
    }

  val addShopItem: Route =
    path("add-shop-item") {
      post {
        entity(as[ShopItem]) { shopItem =>
          onSuccess(addItem(shopItem)) {
            case ItemAdded => complete(s"New item with uuid ${shopItem.uuid} was added")
            case _ => complete(BadRequest, s"failed")
          }
        }
      }
    }

  val routes: Route = userRegistration ~ addShopItem

  val userService: ActorRef
  val warehouseService: ActorRef

  def registerUser(user: User): Future[UserRegistrationResponse] =
    userService.ask(RegisterUser(user)).mapTo[UserRegistrationResponse]

  def authenticate(user: User): Future[UserAuthenticationResponse] =
    userService.ask(AuthUser(user)).mapTo[UserAuthenticationResponse]

  def addItem(shopItem: ShopItem): Future[WarehouseServiceResponse] =
    warehouseService.ask(AddItem(shopItem)).mapTo[WarehouseServiceResponse]
}

class ShopApi(system: ActorSystem, timeout: Timeout) extends ShopRoute with JsonSupport {
  implicit val requestTimeout: Timeout = timeout
  lazy val userService: ActorRef = system.actorOf(Props[UserService])
  lazy val warehouseService: ActorRef = system.actorOf(Props[WarehouseService])
}

object JwtAuthenticationObj extends JwtAuthentication {
  case class Token(data: String)

  override val jwtConfig: JwtConfig = new JwtConfig(ConfigFactory.load())

  def auth(): Directive1[Token] = jwtAuthenticate(Unmarshaller.strict(x => Token(x)));
}
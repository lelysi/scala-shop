package lelysi.scalashop

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Route
import model.{ShopItem, User}
import spray.json.{DefaultJsonProtocol, JsArray, JsNumber, JsString, JsValue, RootJsonFormat}
import akka.http.scaladsl.server.Directives._
import lelysi.scalashop.service.{UserService, WarehouseService}
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.pattern.ask
import lelysi.scalashop.service.UserService.{EmailAlreadyUsed, RegisterUser, UserRegistered, UserServiceResponse}
import lelysi.scalashop.service.WarehouseService.{AddItem, ItemAdded}
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.Future

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat2(User)

  implicit val shopItemFormat: RootJsonFormat[ShopItem] = new RootJsonFormat[ShopItem] {
    override def read(json: JsValue): ShopItem = json.asJsObject().getFields("price", "description") match {
      case Seq(JsNumber(price), JsString(description)) =>
        new ShopItem(price.toDouble, description.toString)
      case _ => throw DeserializationException("ShopItem expected" + json)
    }
    override def write(obj: ShopItem): JsValue = JsArray(JsNumber(obj.price), JsString(obj.description))
  }
}

trait ShopRoute {
  this: JsonSupport =>

  val userRegistration: Route =
    path("registration") {
      post {
        entity(as[User]) { user =>
          onSuccess(registerUser(user)) {
            case UserRegistered() => complete(s"New user with email ${user.email} was registered")
            case EmailAlreadyUsed() => complete(BadRequest, s"user already exists!")
            case _ => complete(BadRequest, s"registration failed")
          }
        }
      }
    }

  val addShopItem: Route =
    path("add-shop-item") {
      post {
        entity(as[ShopItem]) { shopItem =>
          onSuccess(addItem(shopItem)) {
            case ItemAdded() => complete(s"New item with uuid ${shopItem.uuid} was added")
            case _ => complete(BadRequest, s"failed")
          }
        }
      }
    }

  val routes: Route = userRegistration ~ addShopItem

  val userService: ActorRef
  val warehouseService: ActorRef

  def registerUser(user: User): Future[UserServiceResponse] =
    userService.ask(RegisterUser(user))(3.seconds).mapTo[UserServiceResponse]

  def addItem(shopItem: ShopItem): Future[ItemAdded] =
    warehouseService.ask(AddItem(shopItem))(3.seconds).mapTo[ItemAdded]
}

class ShopApi(system: ActorSystem) extends ShopRoute with JsonSupport {
  lazy val userService: ActorRef = system.actorOf(UserService.props)
  lazy val warehouseService: ActorRef = system.actorOf(WarehouseService.props)
}
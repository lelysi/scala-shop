package lelysi.scalashop

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import lelysi.scalashop.model.{AddItemToWarehouse, Email, ItemUuid, PaymentAccount, ShopItem, User, UserLogin}
import spray.json.{DefaultJsonProtocol, DeserializationException, JsArray, JsNumber, JsString, JsValue, RootJsonFormat}
import com.github.t3hnar.bcrypt._

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val userLoginFormat: RootJsonFormat[UserLogin] = new RootJsonFormat[UserLogin] {
    override def read(json: JsValue): UserLogin = json.asJsObject().getFields("email", "password") match {
      case Seq(JsString(email), JsString(password)) => UserLogin(Email(email), password)
      case _ => throw DeserializationException("User data invalid: " + json)
    }
    override def write(obj: UserLogin): JsValue = JsArray(JsString(obj.email.toString))
  }

  implicit val userFormat: RootJsonFormat[User] = new RootJsonFormat[User] {
    override def read(json: JsValue): User = json.asJsObject().getFields("email", "password", "account") match {
      case Seq(JsString(email), JsString(password), JsString(account)) =>
        val hash = password.bcryptSafe(generateSalt).get
        User(Email(email), hash, PaymentAccount(account))
      case _ => throw DeserializationException("User data invalid: " + json)
    }
    override def write(obj: User): JsValue = JsArray(JsString(obj.email.toString))
  }

  implicit val shopItemFormat: RootJsonFormat[AddItemToWarehouse] = new RootJsonFormat[AddItemToWarehouse] {
    override def read(json: JsValue): AddItemToWarehouse = json.asJsObject().getFields("price", "description", "count") match {
      case Seq(JsNumber(price), JsString(description), JsNumber(count)) =>
        AddItemToWarehouse(new ShopItem(price.toDouble, description.toString), count.toInt)
      case _ => throw DeserializationException("AddItemToWarehouse invalid: " + json)
    }
    override def write(obj: AddItemToWarehouse): JsValue =
      JsArray(JsString(obj.shopItem.uuid.toString), JsNumber(obj.shopItem.price), JsString(obj.shopItem.description))
  }

  implicit val itemUuidFormat: RootJsonFormat[ItemUuid] = new RootJsonFormat[ItemUuid] {
    override def read(json: JsValue): ItemUuid = json.asJsObject().getFields("uuid") match {
      case Seq(JsString(uuid)) => ItemUuid(UUID.fromString(uuid))
      case _ => throw DeserializationException("ItemUuid invalid: " + json)
    }
    override def write(obj: ItemUuid): JsValue = JsArray(JsString(obj.uuid.toString))
  }
}

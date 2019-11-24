package lelysi.scalashop

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, post}
import akka.http.scaladsl.server.Route
import model.User
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat2(User)
}

trait ShopRoute {
  this: JsonSupport =>

  val userRegistration: Route =
    path("registration") {
      post {
        entity(as[User]) { user =>
          complete(s"New user with email ${user.email} was registered")
        }
      }
    }
}
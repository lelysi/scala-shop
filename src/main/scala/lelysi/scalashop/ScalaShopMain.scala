package lelysi.scalashop

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, post}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
//import lelysi.scalashop.model.User
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class User(email: String, password: String)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat2(User)
}

trait ShopRoute {
  this: JsonSupport =>
  val userRegistration: Route =
    path("registration") {
      post {
        entity(as[User]) {
          user => complete(s"New user with email ${user.email} was registered")
        }
      }
    }
}

object ScalaShopMain extends App with JsonSupport with ShopRoute {
  implicit val system: ActorSystem = ActorSystem("scala-shop")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val log: LoggingAdapter = Logging(system, "main")

  val port = 8080

  val bindingFuture =
    Http().bindAndHandle(Route.handlerFlow(userRegistration), "localhost", port)

  log.info(s"Server started at the port $port")
}
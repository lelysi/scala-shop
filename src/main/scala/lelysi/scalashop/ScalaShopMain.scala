package lelysi.scalashop

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

object ScalaShopMain extends App
  with JsonSupport
  with ShopRoute {

  implicit val system: ActorSystem = ActorSystem("scala-shop")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val log: LoggingAdapter = Logging(system, "main")

  val port = 8080

  val bindingFuture = Http().bindAndHandle(Route.handlerFlow(userRegistration), "localhost", port)

  log.info(s"Server started at the port $port")
}
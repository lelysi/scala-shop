package lelysi.scalashop

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

object ScalaShopMain extends App
  with JsonSupport
  with ShopRoute {

  val config = ConfigFactory.load()
  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  implicit val system: ActorSystem = ActorSystem("scala-shop")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val log: LoggingAdapter = Logging(system, "main")

  val bindingFuture = Http().bindAndHandle(Route.handlerFlow(userRegistration), host, port)

  log.info(s"Server started at the port $port")
}
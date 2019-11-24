package example

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http

object SimpleHttp extends App {

  implicit val system: ActorSystem = ActorSystem("simple-http")

  implicit val log: LoggingAdapter = Logging(system, "main")

  val port = 8080

  val bindingFuture =
    Http().bindAndHandle(HealthRoute.healthRoute, "localhost", port)

  log.info(s"Server started at the port $port")
}
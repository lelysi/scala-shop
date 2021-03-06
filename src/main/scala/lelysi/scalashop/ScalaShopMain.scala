package lelysi.scalashop

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

trait RequestTimeout {
  import scala.concurrent.duration._
  def requestTimeout(implicit config: Config): Timeout = {
    val t = config.getString("requestTimeout")
    val d = Duration(t)
    FiniteDuration(d.length, d.unit)
  }
}

object ScalaShopMain extends App with RequestTimeout {

  implicit val config: Config = ConfigFactory.load()
  implicit val timeout: Timeout = requestTimeout
  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  implicit val system: ActorSystem = ActorSystem("scala-shop")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val log: LoggingAdapter = Logging(system, "main")

  val api = new ShopApi(system)

  val bindingFuture = Http().bindAndHandle(Route.handlerFlow(api.routes), host, port)

  log.info(s"Server started at the port $port")
}
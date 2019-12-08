package lelysi.scalashop

import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{Matchers, WordSpec}
import scala.concurrent.duration._

abstract class FunctionalTestSpec extends WordSpec
  with Matchers
  with ScalatestRouteTest
  with TestHelper {

  implicit val config: Config = ConfigFactory.load()
  implicit val timeout: Timeout = Timeout(3.second)
  val url: String
}

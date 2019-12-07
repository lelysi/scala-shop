package lelysi.scalashop.functional

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import lelysi.scalashop.ShopApi
import org.scalatest._
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._

class AddShopItemSpec extends WordSpec
  with Matchers
  with ScalatestRouteTest {

  val url: String = "/add-shop-item"
  implicit val config: Config = ConfigFactory.load()
  val route: Route = new ShopApi(system, Timeout(3.second)).addShopItem()

  lazy val correctEntity = HttpEntity(
    ContentTypes.`application/json`,
    """{ "price": 4.12, "description" : "special offer" }"""
  )

  lazy val incorrectEntity = HttpEntity(
    ContentTypes.`application/json`,
    """{ "price" : "small one" }"""
  )

  "Add shop item" should {
    "return 200 for correct data" in {
      Post(url, correctEntity) ~> route ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

    "return 400 for incorrect register data" in {
      Post(url, incorrectEntity) ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
      }
    }
  }
}

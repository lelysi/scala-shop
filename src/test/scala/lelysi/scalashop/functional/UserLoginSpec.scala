package lelysi.scalashop.functional

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import lelysi.scalashop.ShopApi
import org.scalatest._

import scala.concurrent.duration._

class UserLoginSpec extends WordSpec
  with Matchers
  with ScalatestRouteTest {

  val url: String = "/login"
  implicit val config: Config = ConfigFactory.load()
  val route = new ShopApi(system, Timeout(3.second))

  lazy val correctEntity = HttpEntity(
    ContentTypes.`application/json`,
    """{ "email": "example@example.com", "password" : "pass", "account" : "any" }"""
  )

  lazy val incorrectEntity = HttpEntity(
    ContentTypes.`application/json`,
    """{ "email": "example2@example.com", "password" : "pass", "account" : "any" }"""
  )

  lazy val incorrectPass = HttpEntity(
    ContentTypes.`application/json`,
    """{ "email": "example@example.com", "password" : "pass2", "account" : "any" }"""
  )

  "User Controller" should {
    Post("/registration", correctEntity) ~> route.userRegistration()

    "return 200 for correct data" in {
      Post(url, correctEntity) ~> route.login() ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

    "return 403 for incorrect login data" in {
      Post(url, incorrectEntity) ~> Route.seal(route.login()) ~> check {
        status shouldEqual StatusCodes.Forbidden
      }
    }

    "return 403 for incorrect password" in {
      Post(url, incorrectPass) ~> Route.seal(route.login()) ~> check {
        status shouldEqual StatusCodes.Forbidden
      }
    }
  }
}

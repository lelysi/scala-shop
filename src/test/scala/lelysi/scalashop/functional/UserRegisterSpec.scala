package lelysi.scalashop.functional

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import lelysi.scalashop.{ShopApi}
import org.scalatest._

class UserRegisterSpec extends WordSpec
  with Matchers
  with ScalatestRouteTest {

  val url: String = "/registration"

  val route: Route = new ShopApi(system).userRegistration

  lazy val correctEntity = HttpEntity(
    ContentTypes.`application/json`,
    """{ "email": "example@example.com", "password" : "pass" }"""
  )

  lazy val incorrectEntity = HttpEntity(
    ContentTypes.`application/json`,
    """{ "password" : "pass" }"""
  )

  lazy val incorrectEmailEntity = HttpEntity(
    ContentTypes.`application/json`,
    """{ "email": "@example.com", "password" : "pass" }"""
  )

  "User Register Service" should {
    "return 200 for correct register data" in {
      Post(url, correctEntity) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldEqual "New user with email example@example.com was registered"
      }
    }

    "return 400 for incorrect register data" in {
      Post(url, incorrectEntity) ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
      }
    }

    "return 400 for not correct email data" in {
      Post(url, incorrectEmailEntity) ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
      }
    }

    "not create same user twice" in {
      Post(url, correctEntity) ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
      }
    }
  }
}

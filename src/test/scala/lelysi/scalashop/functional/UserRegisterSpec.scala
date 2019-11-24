package lelysi.scalashop.functional

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import lelysi.scalashop.{JsonSupport, ShopRoute}
import org.scalatest._

class UserRegisterSpec extends WordSpec
  with Matchers
  with ScalatestRouteTest
  with JsonSupport
  with ShopRoute {

  val url: String = "/registration"

  lazy val correctEntity = HttpEntity(
    ContentTypes.`application/json`,
    """{ "email": "example@example.com", "password" : "pass" }"""
  )

  lazy val incorrectEntity = HttpEntity(
    ContentTypes.`application/json`,
    """{ "password" : "pass" }"""
  )

  "User Register Service" should {
    "return 200 for correct register data" in {
      Post(url, correctEntity) ~> userRegistration ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldEqual "New user with email example@example.com was registered"
      }
    }

    "return 400 for incorrect register data" in {
      Post(url, incorrectEntity) ~> Route.seal(userRegistration) ~> check {
        status shouldEqual StatusCodes.BadRequest
      }
    }
  }
}

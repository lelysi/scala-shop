package lelysi.scalashop.functional

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import lelysi.scalashop.{FunctionalTestSpec, ShopApi}

final class UserRegisterSpec extends FunctionalTestSpec {

  lazy val url: String = "/registration"
  lazy val route: Route = new ShopApi(system).userRegistration()

  lazy val correctEntity = HttpEntity(
    ContentTypes.`application/json`,
    """{ "email": "example@example.com", "password" : "pass", "account" : "any" }"""
  )

  lazy val incorrectEntity = HttpEntity(
    ContentTypes.`application/json`,
    """{ "password" : "pass" }"""
  )

  lazy val incorrectEmailEntity = HttpEntity(
    ContentTypes.`application/json`,
    """{ "email": "@example.com", "password" : "pass", "account" : "any" }"""
  )

  "User Register Service" should {
    "return 200 for correct register data" in {
      Post(url, correctEntity) ~> route ~> check {
        status shouldEqual StatusCodes.OK
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

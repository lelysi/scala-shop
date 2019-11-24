package lelysi.scalashop.functional

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.ScalatestRouteTest
import lelysi.scalashop.{JsonSupport, ShopRoute}
import org.scalatest._

class UserRegisterSpec extends WordSpec
  with Matchers
  with ScalatestRouteTest
  with JsonSupport
  with Directives
  with ShopRoute {

  val entity = HttpEntity(
    ContentTypes.`application/json`,
    """{ "email": "example@example.com", "password" : "pass" }"""
  )

  "User Register Service" should {
    "return 200 for correct register data" in {
      Post("/registration", entity) ~> userRegistration ~> check {
        responseAs[String] shouldEqual "New user with email example@example.com was registered"
      }
    }
  }
}

package lelysi.scalashop.functional

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import org.scalatest._

class UserRegisterSpec extends WordSpec with Matchers with ScalatestRouteTest {

  "User Register Service" should {
    "return 200 for correct register data" in {
      Post("/", HttpEntity(ByteString("""{ "email": "example@example.com", "password" : "pass" }"""))) ~>
        Route.UserRegistration ~> check {
        responseAs[String] shouldEqual "New user with email example@example.com was registered"
      }
    }
  }
}

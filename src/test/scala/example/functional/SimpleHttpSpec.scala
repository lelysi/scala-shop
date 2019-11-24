package example.functional

import akka.http.scaladsl.model.StatusCodes
import org.scalatest._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import example.HealthRoute

class SimpleHttpSpec extends WordSpec with Matchers with ScalatestRouteTest {
  "Health Route" should {
    "return 200 for GET request to the root path" in {
      Get("/health") ~> HealthRoute.healthRoute ~> check {
        status shouldBe StatusCodes.OK
      }
    }
    "return 404 for GET request to the wrong root path" in {
      Get("/notFound") ~> Route.seal(HealthRoute.healthRoute) ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }
  }
}

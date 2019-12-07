package lelysi.scalashop.functional

import akka.actor.ActorRef
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import lelysi.scalashop.{JwtAuthentication, ShopApi}
import lelysi.scalashop.model.{Email, PaymentAccount}
import lelysi.scalashop.service.CheckoutService.CheckoutSuccess
import lelysi.scalashop.service.paymentgate.Order
import org.scalatest._
import pdi.jwt.JwtClaim
import scala.concurrent.duration._
import scala.util.Try

class CheckoutSpec extends WordSpec
  with Matchers
  with ScalatestRouteTest {

  implicit val config: Config = ConfigFactory.load()
  lazy val timeout: Timeout = Timeout(3.seconds)

  val url: String = "/checkout"

  val checkoutServiceMock = TestProbe();

  class JwtMock extends JwtAuthentication(config) {
    override def getToken(str: String): String = str
    override def getDecodedClaim(str: String): Try[JwtClaim] = Try(JwtClaim(content = str))
  }

  object MockApi extends ShopApi(system, timeout) {
    override lazy val checkoutService: ActorRef = checkoutServiceMock.ref
    override lazy val jwtAuthenticator: JwtAuthentication = new JwtMock()
  }

  "Checkout products" should {
    "return 200 on valid request" in {
      val result = Post(url) ~>
        addHeader(RawHeader("X-Api-Key", "example@example.com")) ~>
        Route.seal(MockApi.checkoutRoute())

      checkoutServiceMock.expectMsg(Email)
      checkoutServiceMock.reply(CheckoutSuccess(new Order(PaymentAccount("any"), List())))

      result ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

  }
}

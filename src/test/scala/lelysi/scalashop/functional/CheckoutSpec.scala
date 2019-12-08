package lelysi.scalashop.functional

import akka.actor.ActorRef
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.testkit.TestProbe
import lelysi.scalashop.{FunctionalTestSpec, JwtAuthentication, ShopApi}
import lelysi.scalashop.model.{Email, PaymentAccount}
import lelysi.scalashop.service.CheckoutService.CheckoutSuccess
import lelysi.scalashop.service.paymentgate.Order
import pdi.jwt.JwtClaim
import scala.util.Try

final class CheckoutSpec extends FunctionalTestSpec {

  lazy val url: String = "/checkout"
  lazy val checkoutServiceMock = TestProbe();

  final class JwtMock extends JwtAuthentication(config) {
    override def getToken(str: String): String = str
    override def getDecodedClaim(str: String): Try[JwtClaim] = Try(JwtClaim(content = str))
  }

  object MockApi extends ShopApi(system) {
    override lazy val checkoutService: ActorRef = checkoutServiceMock.ref
    override lazy val jwtAuthenticator: JwtAuthentication = new JwtMock()
  }

  "Checkout products" should {
    "return 200 on valid request" in {
      val result = Post(url) ~>
        addHeader(RawHeader("X-Api-Key", "example@example.com")) ~>
        Route.seal(MockApi.checkoutRoute())

      serviceMsgMap(checkoutServiceMock, Email, CheckoutSuccess(new Order(PaymentAccount("any"), List())))

      result ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

  }
}

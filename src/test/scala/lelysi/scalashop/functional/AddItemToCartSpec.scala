package lelysi.scalashop.functional

import java.util.UUID
import akka.actor.ActorRef
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import lelysi.scalashop.ShopApi
import lelysi.scalashop.model.{Email, ItemToCart}
import lelysi.scalashop.service.CartService.{ItemAddedToCart, ItemWasNotFound}
import org.scalatest._
import akka.util.Timeout
import scala.concurrent.duration._

class AddItemToCartSpec extends WordSpec
  with Matchers
  with ScalatestRouteTest {

  lazy val timeout: Timeout = Timeout(3.seconds)

  val url: String = "/add-item-to-cart"

  val probe = TestProbe();

  object MockApi extends ShopApi(system, timeout) {
    override lazy val cartService: ActorRef = probe.ref
  }

  lazy val entity = HttpEntity(
    ContentTypes.`application/json`,
    """{ "uuid" : "b1c8b8fb-3ef7-49ab-907d-dcc4996eac8f" }"""
  )

  "Add item to cart" should {
    "return 201 for correct data" in {
      Post("/registration", HttpEntity(
        ContentTypes.`application/json`,
        """{ "email": "example@example.com", "password" : "1234" }"""
      )) ~> Route.seal(MockApi.userRegistration) ~> check {
        status shouldEqual StatusCodes.OK
      }

      val jwtKey = Post("/login", HttpEntity(
        ContentTypes.`application/json`,
        """{ "email": "example@example.com", "password" : "1234" }"""
      )) ~> Route.seal(MockApi.login) ~> check {
        response.entity.toStrict(timeout.duration).map(_.data.decodeString("UTF-8"))
      }

      val result =
        Post(url, entity) ~>
        addHeader(RawHeader("X-Api-Key", jwtKey.value.get.get)) ~>
        Route.seal(MockApi.addItemToCartRoute())

      probe.expectMsg(ItemToCart(Email("example@example.com"), UUID.fromString("b1c8b8fb-3ef7-49ab-907d-dcc4996eac8f")))
      probe.reply(ItemAddedToCart)

      result ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldEqual s"item added"
      }
    }

    "return 401 for invalid auth" in {
      Post(url, entity) ~>
        addHeader(RawHeader("X-Api-Key", "thisiscompletelyincorrecttoken")) ~>
        Route.seal(MockApi.addItemToCartRoute()) ~>
        check {
          status shouldEqual StatusCodes.Unauthorized
          responseAs[String] shouldEqual s"invalid key"
      }
    }

    "return 400 for incorrect item data" in {
      val jwtKey = Post("/login", HttpEntity(
        ContentTypes.`application/json`,
        """{ "email": "example@example.com", "password" : "1234" }"""
      )) ~> Route.seal(MockApi.login) ~> check {
        response.entity.toStrict(timeout.duration).map(_.data.decodeString("UTF-8"))
      }

      val result =
        Post(url, entity) ~>
          addHeader(RawHeader("X-Api-Key", jwtKey.value.get.get)) ~>
          Route.seal(MockApi.addItemToCartRoute())

      probe.expectMsg(ItemToCart(Email("example@example.com"), UUID.fromString("b1c8b8fb-3ef7-49ab-907d-dcc4996eac8f")))
      probe.reply(ItemWasNotFound)

      result ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[String] shouldEqual s"item was not found"
      }
    }

  }
}

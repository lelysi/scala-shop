package lelysi.scalashop.unit

import java.util.UUID
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import lelysi.scalashop.StopSystemAfterAll
import lelysi.scalashop.model.{Email, ShopItem}
import lelysi.scalashop.service.CartService.{ItemAddedToCart, ItemToCart, ItemWasNotFound}
import lelysi.scalashop.service.CartService
import lelysi.scalashop.service.WarehouseService.{GetItem, ItemFound}
import org.scalatest.WordSpecLike

class CartServiceSpec extends TestKit(ActorSystem("test-spec"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll {

  lazy val existingUuidInWarehouse: UUID = UUID.fromString("b1c8b8fb-3ef7-49ab-907d-dcc4996eac8f")
  lazy val nonExistingUuidInWarehouse: UUID = UUID.fromString("00000000-3ef7-49ab-907d-dcc4996eac8f")

  lazy val correctItemToCart = ItemToCart(Email("example@example.com"), existingUuidInWarehouse)
  lazy val incorrectItemToCart = ItemToCart(Email("example@example.com"), nonExistingUuidInWarehouse)

  val probe = TestProbe();

  "Cart Service" should {
    val cartService = system.actorOf(CartService.props(probe.ref))
    "response ItemAdded on existing item" in {
      cartService ! correctItemToCart
      probe.expectMsg(GetItem(existingUuidInWarehouse))
      probe.reply(ItemFound(ShopItem(existingUuidInWarehouse, 40.0, "")))
      expectMsg(ItemAddedToCart)
    }
    "response ItemWasNotFound on non existing item" in {
      cartService ! incorrectItemToCart
      probe.expectMsg(GetItem(nonExistingUuidInWarehouse))
      probe.reply(ItemWasNotFound)
      expectMsg(ItemWasNotFound)
    }
  }
}
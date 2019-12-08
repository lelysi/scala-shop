package lelysi.scalashop.unit.service

import akka.actor.ActorRef
import lelysi.scalashop.model.Email
import lelysi.scalashop.service.CartService
import lelysi.scalashop.service.CartService.{Cart, CartNotFound, EmptyCart, GetCart, ItemAddedToCart, ItemToCart, ItemWasNotFound}
import lelysi.scalashop.service.WarehouseService.{GetItem, ItemFound}
import lelysi.scalashop.unit.UnitServiceSpec

final class CartServiceSpec extends UnitServiceSpec {

  lazy val correctItemToCart = ItemToCart(correctEmail, storedInWarehouseItemUuid)
  lazy val incorrectItemToCart = ItemToCart(correctEmail, notStoredInWarehouseItemUuid)
  lazy val cartService: ActorRef = system.actorOf(CartService.props(warehouseServiceMock.ref))

  "Cart Service" should {
    "response ItemAdded on existing item" in {
      cartService ! correctItemToCart
      serviceMsgMap(warehouseServiceMock, GetItem(storedInWarehouseItemUuid, 1), ItemFound(storedItem))
      expectMsg(ItemAddedToCart)
    }

    "response ItemWasNotFound on not existing item" in {
      cartService ! incorrectItemToCart
      serviceMsgMap(warehouseServiceMock, GetItem(notStoredInWarehouseItemUuid, 1), ItemWasNotFound)
      expectMsg(ItemWasNotFound)
    }

    "response Cart on GetCart for existing cart" in {
      cartService ! GetCart(correctEmail)
      expectMsg(Cart(List(storedItem)))
    }

    "response CartNotFound on GetCart for not existing cart" in {
      cartService ! GetCart(Email("example2@example.com"))
      expectMsg(CartNotFound)
    }

    "response CartNotFound after removing cart" in {
      cartService ! EmptyCart(correctEmail)
      cartService ! GetCart(correctEmail)
      expectMsg(CartNotFound)
    }
  }
}
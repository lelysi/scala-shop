package lelysi.scalashop.unit.service

import akka.actor.{ActorRef, Props}
import akka.testkit.TestProbe
import lelysi.scalashop.model.{Email, PaymentAccount, ShopItem, User}
import lelysi.scalashop.service.CartService.{Cart, CartNotFound, EmptyCart, GetCart}
import lelysi.scalashop.service.CheckoutService
import lelysi.scalashop.service.CheckoutService.{CheckoutFail, CheckoutSuccess}
import lelysi.scalashop.service.UserService.{FindUser, UserFound, UserUnknown}
import lelysi.scalashop.service.WarehouseService.{CheckItemList, ItemsAvailable, ItemsNotAvailable, RemoveItemList}
import lelysi.scalashop.service.paymentgate.{FakePaymentGate, Order}
import lelysi.scalashop.unit.UnitServiceSpec

final class CheckoutServiceSpec extends UnitServiceSpec {

  lazy val userService = TestProbe()
  lazy val cartService = TestProbe()
  lazy val paymentAccount = PaymentAccount("valid-data")
  lazy val existingUser = User(correctEmail, "any-string-for-hash", paymentAccount)
  lazy val item1 = new ShopItem(20.0, "dead fish from Luca Brasi friends")
  lazy val item2 = new ShopItem(1_200.0, "horse head for american producer")
  lazy val listOfItems: List[ShopItem] = List(item1, item2, item1)
  lazy val checkoutService: ActorRef = system.actorOf(CheckoutService.props(
    userService.ref, cartService.ref, warehouseServiceMock.ref, system.actorOf(Props[FakePaymentGate])
  ))

  "Checkout Service with Fake Payment Gate" should {
    "response on correct msg" in {
      checkoutService ! correctEmail
      serviceMsgMap(userService, FindUser(correctEmail), UserFound(existingUser))
      serviceMsgMap(cartService, GetCart(correctEmail), Cart(listOfItems))
      serviceMsgMap(warehouseServiceMock, CheckItemList(listOfItems), ItemsAvailable)
      expectMsgPF() {
        case response @ CheckoutSuccess(Order(_, account, items))
          if account == paymentAccount & items == listOfItems => response
      }
      cartService.expectMsg(EmptyCart(correctEmail))
      warehouseServiceMock.expectMsg(RemoveItemList(listOfItems))
    }

    "response on unknown user" in {
      checkoutService ! correctEmail
      serviceMsgMap(userService, FindUser(correctEmail), UserUnknown)
      serviceMsgMap(cartService, GetCart(correctEmail), Cart(listOfItems))
      serviceMsgMap(warehouseServiceMock, CheckItemList(listOfItems), ItemsAvailable)
      expectMsg(CheckoutFail)
    }

    "response on empty cart" in {
      checkoutService ! correctEmail
      serviceMsgMap(userService, FindUser(correctEmail), UserFound(existingUser))
      serviceMsgMap(cartService, GetCart(correctEmail), CartNotFound)
      expectMsg(CheckoutFail)
    }

    "response on empty warehouse" in {
      checkoutService ! correctEmail
      serviceMsgMap(userService, FindUser(correctEmail), UserFound(existingUser))
      serviceMsgMap(cartService, GetCart(correctEmail), Cart(listOfItems))
      serviceMsgMap(warehouseServiceMock, CheckItemList(listOfItems), ItemsNotAvailable)
      expectMsg(CheckoutFail)
    }
  }
}
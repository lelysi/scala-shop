package lelysi.scalashop.unit

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import lelysi.scalashop.StopSystemAfterAll
import lelysi.scalashop.model.{Email, PaymentAccount, ShopItem, User}
import lelysi.scalashop.service.CartService.{Cart, CartNotFound, EmptyCart, GetCart}
import lelysi.scalashop.service.CheckoutService
import lelysi.scalashop.service.CheckoutService.{CheckoutFail, CheckoutSuccess}
import lelysi.scalashop.service.UserService.{FindUser, UserFound, UserUnknown}
import lelysi.scalashop.service.WarehouseService.{CheckItemList, ItemsAvailable, ItemsNotAvailable, RemoveItemList}
import lelysi.scalashop.service.paymentgate.{FakePaymentGate, Order}
import org.scalatest.WordSpecLike

class CheckoutServiceSpec extends TestKit(ActorSystem("test-spec"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll {

  val userService = TestProbe()
  val cartService = TestProbe()
  val warehouseService = TestProbe()

  val email = Email("example@example.com")
  val paymentAccount = PaymentAccount("valid-data")
  val existingUser = User(email, "any-string-for-hash", paymentAccount)
  val item1 = new ShopItem(20.0, "dead fish from Luca Brasi friends")
  val item2 = new ShopItem(1_200.0, "horse head for american producer")
  val listOfItems: List[ShopItem] = List(item1, item2, item1)

  def serviceMsgMap(service: TestProbe, income: AnyRef, response: AnyRef): Unit = {
    service.expectMsg(income)
    service.reply(response)
  }

  "Checkout Service with Fake Payment Gate" should {
    val checkoutService = system.actorOf(CheckoutService.props(
      userService.ref, cartService.ref, warehouseService.ref, system.actorOf(Props[FakePaymentGate])
    ))

    "response on correct msg" in {
      checkoutService ! email
      serviceMsgMap(userService, FindUser(email), UserFound(existingUser))
      serviceMsgMap(cartService, GetCart(email), Cart(listOfItems))
      serviceMsgMap(warehouseService, CheckItemList(listOfItems), ItemsAvailable)
      expectMsgPF() {
        case response @ CheckoutSuccess(Order(_, account, items))
          if account == paymentAccount & items == listOfItems => response
      }
      cartService.expectMsg(EmptyCart(email))
      warehouseService.expectMsg(RemoveItemList(listOfItems))
    }

    "response on unknown user" in {
      checkoutService ! email
      serviceMsgMap(userService, FindUser(email), UserUnknown)
      serviceMsgMap(cartService, GetCart(email), Cart(listOfItems))
      serviceMsgMap(warehouseService, CheckItemList(listOfItems), ItemsAvailable)
      expectMsg(CheckoutFail)
    }

    "response on empty cart" in {
      checkoutService ! email
      serviceMsgMap(userService, FindUser(email), UserFound(existingUser))
      serviceMsgMap(cartService, GetCart(email), CartNotFound)
      expectMsg(CheckoutFail)
    }

    "response on empty warehouse" in {
      checkoutService ! email
      serviceMsgMap(userService, FindUser(email), UserFound(existingUser))
      serviceMsgMap(cartService, GetCart(email), Cart(listOfItems))
      serviceMsgMap(warehouseService, CheckItemList(listOfItems), ItemsNotAvailable)
      expectMsg(CheckoutFail)
    }
  }
}
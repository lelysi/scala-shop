package lelysi.scalashop.service

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import lelysi.scalashop.RequestTimeout
import lelysi.scalashop.model.{Email, ShopItem}
import lelysi.scalashop.service.CartService.{Cart, CartNotFound, CartResponse, EmptyCart, GetCart}
import lelysi.scalashop.service.CheckoutService.{CheckoutFail, CheckoutRequest, CheckoutSuccess}
import lelysi.scalashop.service.UserService.{FindUser, UserFound, UserSearchResponse, UserUnknown}
import lelysi.scalashop.service.WarehouseService.{CheckItemList, CheckItemResult, ItemsAvailable, RemoveItemList}
import lelysi.scalashop.service.paymentgate.{Order, OrderCompleted, PaymentGateResponse}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.util.Success

object CheckoutService {
  def props(userService: ActorRef, cartService: ActorRef, warehouseService: ActorRef, paymentGate: ActorRef) =
    Props(new CheckoutService(userService, cartService, warehouseService, paymentGate))

  case class CheckoutRequest(email: Email)

  sealed trait CheckoutServiceResponse
  case class CheckoutSuccess(order: Order) extends CheckoutServiceResponse
  case object CheckoutFail extends CheckoutServiceResponse
}

class CheckoutService(
  userService: ActorRef,
  cartService: ActorRef,
  warehouseService: ActorRef,
  paymentGate: ActorRef
) extends Actor with RequestTimeout {
  implicit val timeout: Timeout = Timeout(3.second)
  implicit val ec: ExecutionContextExecutor = context.system.getDispatcher

  override def receive: Receive = {
    case CheckoutRequest(email) =>
      val senderActor = sender()
      val userFutureResult = userService.ask(FindUser(email)).mapTo[UserSearchResponse]
      val cartFutureResult = cartService.ask(GetCart(email)).mapTo[CartResponse]

      def checkItemsInWarehouse(items: List[ShopItem]): Future[CheckItemResult] =
        warehouseService.ask(CheckItemList(items)).mapTo[CheckItemResult]

      def processCheckout(
        userResponse: UserService.UserSearchResponse,
        warehouseResponse: WarehouseService.WarehouseServiceResponse,
        items: List[ShopItem]
      ): Unit = {
        (userResponse, warehouseResponse) match {
          case (UserFound(user), ItemsAvailable) =>
            val order = new Order(user.paymentAccount, items)
            val gateFutureResult = paymentGate.ask(order).mapTo[PaymentGateResponse]
            gateFutureResult onComplete {
              case Success(result) => result match {
                case OrderCompleted =>
                  senderActor ! CheckoutSuccess(order)
                  cartService ! EmptyCart(user.email)
                  warehouseService ! RemoveItemList(items)

                case _ => senderActor ! CheckoutFail
              }
              case _ => senderActor ! CheckoutFail
            }
          case _ => senderActor ! CheckoutFail
        }
      }

      cartFutureResult onComplete {
        case Success(result) => result match {
          case Cart(items) =>
            for {
              warehouseResult <- checkItemsInWarehouse(items)
              userResult <- userFutureResult
            } yield processCheckout(userResult, warehouseResult, items)

          case CartNotFound => senderActor ! CheckoutFail
        }
        case _ => senderActor ! CheckoutFail
      }
  }
}
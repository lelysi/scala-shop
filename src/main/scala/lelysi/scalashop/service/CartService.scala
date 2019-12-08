package lelysi.scalashop.service

import java.util.UUID
import akka.actor.{Actor, ActorRef, Props}
import akka.event.{Logging, LoggingAdapter}
import lelysi.scalashop.model.{Email, ShopItem}
import lelysi.scalashop.service.CartService.{Cart, CartNotFound, EmptyCart, GetCart, ItemAddedToCart, ItemToCart, ItemWasNotFound}
import lelysi.scalashop.service.WarehouseService.{GetItem, ItemFound, ItemNotFound, WarehouseServiceGetItemResponse}
import akka.pattern.ask
import lelysi.scalashop.RequestTimeout
import scala.collection.mutable
import scala.concurrent.ExecutionContextExecutor
import scala.util.Success
import akka.util.Timeout
import scala.concurrent.duration._

object CartService {
  def props(warehouseService: ActorRef) = Props(new CartService(warehouseService))

  case class GetCart(userEmail: Email)
  case class EmptyCart(userEmail: Email)
  case class ItemToCart(userEmail: Email, uuid: UUID)

  sealed trait CartServiceResponse
  sealed trait ItemAddingResponse extends CartServiceResponse
  case object ItemAddedToCart extends ItemAddingResponse
  case object ItemWasNotFound extends ItemAddingResponse

  sealed trait CartResponse extends CartServiceResponse
  case object CartNotFound extends CartResponse
  case class Cart(items: List[ShopItem]) extends CartResponse
}

class CartService(warehouseService: ActorRef) extends Actor with RequestTimeout {
  private val cartRepository: mutable.Map[Email, List[ShopItem]] = mutable.Map()
  implicit val timeout: Timeout = Timeout(3.second)
  implicit val ec: ExecutionContextExecutor = context.system.getDispatcher
  implicit val log: LoggingAdapter = Logging(context.system, "main")

  override def receive: Receive = {
    case ItemToCart(userEmail, uuid) =>
      val senderActor = sender()
      val alreadyAddedItems = cartRepository.get(userEmail) match {
        case Some(items) => items.count(item => item.uuid == uuid)
        case None => 0
      }
      warehouseService.ask(GetItem(uuid, alreadyAddedItems + 1)) onComplete {
        case Success(optionResult: WarehouseServiceGetItemResponse) => optionResult match {
          case ItemFound(shopItem) =>
            cartRepository.get(userEmail) match {
              case Some(userCart) => cartRepository.update(userEmail, userCart :+ shopItem)
              case None => cartRepository(userEmail) = List(shopItem)
            }
            senderActor ! ItemAddedToCart
          case ItemNotFound => senderActor ! ItemWasNotFound
        }
        case _ => senderActor ! ItemWasNotFound
      }

    case GetCart(userEmail) =>
      cartRepository.get(userEmail) match {
        case Some(userCart) => sender() ! Cart(userCart)
        case None => sender() ! CartNotFound
      }

    case EmptyCart(userEmail) =>
      cartRepository.remove(userEmail) match {
        case None => log.warning(s"Request to remove data from empty cart repository by email $userEmail")
        case _ => log.info(s"User $userEmail cart repository is empty")
      }
  }
}
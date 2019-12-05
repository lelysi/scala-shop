package lelysi.scalashop.service

import akka.actor.{Actor, ActorRef, Props}
import lelysi.scalashop.model.{Email, ItemToCart, ShopItem}
import lelysi.scalashop.service.CartService.{ItemAddedToCart, ItemWasNotFound}
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
  sealed trait CartServiceResponse
  case object ItemAddedToCart extends CartServiceResponse
  case object ItemWasNotFound extends CartServiceResponse
}

class CartService(warehouseService: ActorRef) extends Actor with RequestTimeout {
  private val cartRepository: mutable.Map[Email, List[ShopItem]] = mutable.Map()
  implicit val timeout: Timeout = Timeout(3.second)
  implicit val ec: ExecutionContextExecutor = context.system.getDispatcher

  override def receive: Receive = {
    case ItemToCart(userEmail, uuid) =>
      val senderActor = sender()
      warehouseService.ask(GetItem(uuid)) onComplete {
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
  }
}
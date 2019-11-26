package lelysi.scalashop.service

import akka.actor.{Actor, Props}
import lelysi.scalashop.model.ShopItem
import lelysi.scalashop.service.WarehouseService.{AddItem, ItemAdded}

import scala.collection.mutable

object WarehouseService {
  def props: Props = Props(new WarehouseService)

  case class AddItem(shopItem: ShopItem)

  case class ItemAdded()
}

class WarehouseService extends Actor {
  private val shopItemRepository: mutable.Map[ShopItem, Int] = mutable.Map()

  override def receive: Receive = {
    case AddItem(shopItem) =>
      shopItemRepository.get(shopItem) match {
        case Some(elem) => shopItemRepository.update(shopItem, elem + 1)
        case None => shopItemRepository(shopItem) = 1
      }
      sender() ! ItemAdded()
  }
}
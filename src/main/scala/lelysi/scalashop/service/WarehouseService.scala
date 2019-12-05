package lelysi.scalashop.service

import java.util.UUID
import akka.actor.Actor
import lelysi.scalashop.model.ShopItem
import lelysi.scalashop.service.WarehouseService.{AddItem, GetItem, ItemAdded, ItemFound, ItemNotFound}
import scala.collection.mutable

object WarehouseService {
  case class AddItem(shopItem: ShopItem)
  case class GetItem(uuid: UUID)

  sealed trait WarehouseServiceResponse
  sealed trait WarehouseServiceAddItemResponse extends WarehouseServiceResponse
  case object ItemAdded extends WarehouseServiceAddItemResponse
  sealed trait WarehouseServiceGetItemResponse extends WarehouseServiceResponse
  case class ItemFound(shopItem: ShopItem) extends WarehouseServiceGetItemResponse
  case object ItemNotFound extends WarehouseServiceGetItemResponse
}

class WarehouseService extends Actor {
  private val shopItemRepository: mutable.Map[UUID, (ShopItem, Int)] = mutable.Map()

  override def receive: Receive = {
    case AddItem(shopItem) =>
      shopItemRepository.get(shopItem.uuid) match {
        case Some((savedItem, count)) => shopItemRepository.update(shopItem.uuid, (savedItem, count + 1))
        case None => shopItemRepository(shopItem.uuid) = (shopItem, 1)
      }
      sender() ! ItemAdded

    case GetItem(uuid) =>
      shopItemRepository.get(uuid) match {
        case Some((savedItem, count)) =>
          if (count > 0) {
            sender() ! ItemFound(savedItem)
          }
          else {
            sender() ! ItemNotFound
          }
        case None => sender() ! ItemNotFound
      }
  }
}
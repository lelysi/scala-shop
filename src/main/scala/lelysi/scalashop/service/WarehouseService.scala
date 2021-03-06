package lelysi.scalashop.service

import java.util.UUID
import akka.actor.Actor
import akka.event.{Logging, LoggingAdapter}
import lelysi.scalashop.model.{AddItemToWarehouse, ShopItem}
import lelysi.scalashop.service.WarehouseService.{AddItem, CheckItemList, GetItem, ItemAdded, ItemFound, ItemNotFound, ItemsAvailable, ItemsNotAvailable, RemoveItemList}
import scala.collection.mutable

object WarehouseService {
  case class AddItem(itemToWarehouse: AddItemToWarehouse)
  case class GetItem(uuid: UUID, count: Int)
  case class CheckItemList(items: List[ShopItem])
  case class RemoveItemList(items: List[ShopItem])
  case object GetAll

  sealed trait WarehouseServiceResponse

  sealed trait WarehouseServiceAddItemResponse extends WarehouseServiceResponse
  case object ItemAdded extends WarehouseServiceAddItemResponse

  sealed trait WarehouseServiceGetItemResponse extends WarehouseServiceResponse
  case class ItemFound(shopItem: ShopItem) extends WarehouseServiceGetItemResponse
  case object ItemNotFound extends WarehouseServiceGetItemResponse

  sealed trait CheckItemResult extends WarehouseServiceResponse
  case object ItemsAvailable extends CheckItemResult
  case object ItemsNotAvailable extends CheckItemResult
}

class WarehouseService extends Actor {
  implicit val log: LoggingAdapter = Logging(context.system, "main")
  private val shopItemRepository: mutable.Map[UUID, (ShopItem, Int)] = mutable.Map()

  override def receive: Receive = {
    case AddItem(itemToWarehouse) =>
      shopItemRepository.get(itemToWarehouse.shopItem.uuid) match {
        case Some((savedItem, count)) =>
          shopItemRepository.update(itemToWarehouse.shopItem.uuid, (savedItem, count + itemToWarehouse.count))
        case None =>
          shopItemRepository(itemToWarehouse.shopItem.uuid) = (itemToWarehouse.shopItem, itemToWarehouse.count)
      }
      sender() ! ItemAdded

    case GetItem(uuid, countToAdd) =>
      shopItemRepository.get(uuid) match {
        case Some((savedItem, count)) =>
          if (count >= countToAdd) {
            sender() ! ItemFound(savedItem)
          }
          else {
            sender() ! ItemNotFound
          }
        case None => sender() ! ItemNotFound
      }

    case CheckItemList(items) =>
      def checkIfItemsInRepository(items: List[ShopItem]): Boolean = {
        val uuidAndCount = items.groupMapReduce(_.uuid)(_ => 1)(_ + _)
        for ((itemUuid, itemCount) <- uuidAndCount) {
          shopItemRepository.get(itemUuid) match {
            case Some((_, count)) => if (count < itemCount) return false
            case None => return false
          }
        }
        true
      }

      if (checkIfItemsInRepository(items)) {
        sender() ! ItemsAvailable
      }
      else {
        sender() ! ItemsNotAvailable
      }

    case RemoveItemList(items) =>
      def removeItemsFromRepository(items: List[ShopItem]): Boolean = {
        val uuidAndCount = items.groupMapReduce(_.uuid)(_ => 1)(_ + _)
        for ((itemUuid, itemCount) <- uuidAndCount) {
          shopItemRepository.get(itemUuid) match {
            case Some((savedItem, count)) =>
              if (itemCount > count)
                log.error(s"Incorrect request to reduce more item than available in warehouse for uuid $itemUuid count $itemCount")
              else shopItemRepository.update(itemUuid, (savedItem, count - itemCount))
            case None =>
              log.error(s"Incorrect request to reduce item amount in warehouse for uuid $itemUuid")
          }
        }
        true
      }

      removeItemsFromRepository(items)
  }
}
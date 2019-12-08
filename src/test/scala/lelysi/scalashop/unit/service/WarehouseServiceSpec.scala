package lelysi.scalashop.unit.service

import akka.actor.{ActorRef, Props}
import lelysi.scalashop.model.{AddItemToWarehouse, ShopItem}
import lelysi.scalashop.service.WarehouseService
import lelysi.scalashop.service.WarehouseService._
import lelysi.scalashop.unit.UnitServiceSpec

final class WarehouseServiceSpec extends UnitServiceSpec {

  lazy val notStoredItem = ShopItem(notStoredInWarehouseItemUuid, 100.00, "poker deck")
  lazy val warehouseService: ActorRef = system.actorOf(Props[WarehouseService])
  lazy val itemToWarehouse = AddItemToWarehouse(storedItem, 1)

  "Warehouse Service" should {
    "send ItemAdded message on AddItem" in {
      warehouseService ! AddItem(itemToWarehouse)
      expectMsg(ItemAdded)
    }

    "send ItemFound on GetItem with correct item" in {
      warehouseService ! GetItem(storedInWarehouseItemUuid, 1)
      expectMsg(ItemFound(storedItem))
    }

    "send ItemFound on GetItem with correct item and incorrect numbers" in {
      warehouseService ! GetItem(storedInWarehouseItemUuid, 2)
      expectMsg(ItemNotFound)
    }

    "send ItemNotFound on GetItem with correct item" in {
      warehouseService ! GetItem(notStoredInWarehouseItemUuid, 1)
      expectMsg(ItemNotFound)
    }

    "send ItemsAvailable on CheckItemList with correct items amount" in {
      // add another one item to storage
      warehouseService ! AddItem(itemToWarehouse)
      expectMsg(ItemAdded)

      warehouseService ! CheckItemList(List(storedItem))
      expectMsg(ItemsAvailable)
    }

    "send ItemsNotAvailable on CheckItemList with incorrect items amount" in {
      warehouseService ! CheckItemList(List(storedItem, storedItem, storedItem))
      expectMsg(ItemsNotAvailable)
    }

    "send ItemsNotAvailable on CheckItemList with incorrect items" in {
      warehouseService ! CheckItemList(List(notStoredItem))
      expectMsg(ItemsNotAvailable)
    }

    "has no such items after removing" in {
      warehouseService ! RemoveItemList(List(storedItem, storedItem))
      warehouseService ! CheckItemList(List(storedItem))
      expectMsg(ItemsNotAvailable)
    }
  }
}
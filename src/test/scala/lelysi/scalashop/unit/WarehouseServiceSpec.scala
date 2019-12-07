package lelysi.scalashop.unit

import java.util.UUID
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import lelysi.scalashop.StopSystemAfterAll
import lelysi.scalashop.model.ShopItem
import lelysi.scalashop.service.WarehouseService
import lelysi.scalashop.service.WarehouseService.{AddItem, CheckItemList, GetItem, ItemAdded, ItemFound, ItemNotFound, ItemsAvailable, ItemsNotAvailable}
import org.scalatest.WordSpecLike

class WarehouseServiceSpec extends TestKit(ActorSystem("test-spec"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll {

  val storedItemUuid: UUID = UUID.fromString("b0780116-f213-4066-afb6-faaf24b399bb")
  val notStoredItemUuid: UUID = UUID.fromString("01234567-89ab-cdef-0123-456789abcdef")
  val storedItem = ShopItem(storedItemUuid, 4.12, "special offer")
  val notStoredItem = ShopItem(notStoredItemUuid, 100.00, "poker deck")

  "Warehouse Service" should {
    val warehouseService = system.actorOf(Props[WarehouseService])
    "send ItemAdded message on AddItem" in {
      warehouseService ! AddItem(storedItem)
      expectMsg(ItemAdded)
    }

    "send ItemFound on GetItem with correct item" in {
      warehouseService ! GetItem(storedItemUuid)
      expectMsg(ItemFound(storedItem))
    }

    "send ItemNotFound on GetItem with correct item" in {
      warehouseService ! GetItem(notStoredItemUuid)
      expectMsg(ItemNotFound)
    }

    "send ItemsAvailable on CheckItemList with correct items amount" in {
      // add another one item to storage
      warehouseService ! AddItem(storedItem)
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
  }
}
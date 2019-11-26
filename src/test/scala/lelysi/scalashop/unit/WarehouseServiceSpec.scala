package lelysi.scalashop.unit

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import lelysi.scalashop.StopSystemAfterAll
import lelysi.scalashop.model.ShopItem
import lelysi.scalashop.service.WarehouseService
import lelysi.scalashop.service.WarehouseService.{AddItem, ItemAdded}
import org.scalatest.WordSpecLike

class WarehouseServiceSpec extends TestKit(ActorSystem("test-spec"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll {

  "Warehouse Service" should {
    val warehouseService = system.actorOf(Props[WarehouseService])
    "send user registered message back" in {
      warehouseService ! AddItem(new ShopItem(4.12, "special offer"))
      expectMsg(ItemAdded)
    }
  }
}
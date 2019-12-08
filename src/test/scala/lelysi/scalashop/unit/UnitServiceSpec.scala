package lelysi.scalashop.unit

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import lelysi.scalashop.TestHelper
import lelysi.scalashop.model.{Email, ShopItem}
import org.scalatest.{BeforeAndAfterAll, Suite, WordSpecLike}

trait StopSystemAfterAll extends BeforeAndAfterAll {
  this: TestKit with Suite =>
  override protected def afterAll() {
    super.afterAll()
    system.terminate()
  }
}

abstract class UnitServiceSpec extends TestKit(ActorSystem("test-spec"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll
  with TestHelper {

  lazy val correctEmail = Email("example@example.com")
  lazy val storedInWarehouseItemUuid: UUID = UUID.fromString("b1c8b8fb-3ef7-49ab-907d-dcc4996eac8f")
  lazy val notStoredInWarehouseItemUuid: UUID = UUID.fromString("01234567-89ab-cdef-0123-456789abcdef")
  lazy val storedItem = ShopItem(storedInWarehouseItemUuid, 40.0, "average programming book")
  lazy val warehouseServiceMock = TestProbe()
}

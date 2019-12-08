package lelysi.scalashop.unit.model

import java.util.UUID
import lelysi.scalashop.model.ShopItem
import org.scalatest._

final class ShopItemSpec extends WordSpec {

  "ShopItem" should {
    "throw an exception on free items" in {
      intercept[AssertionError] {
        // if we decide to have free items in a shop - we'll need to change that
        ShopItem(UUID.randomUUID(), 0.0, "very expensive book")
      }
    }

    "throw an exception on incorrect data" in {
      intercept[AssertionError] {
        ShopItem(UUID.randomUUID(), -10.0, "we pay you for it")
      }
    }

    "be ok" in {
      ShopItem(UUID.randomUUID(), 10.0, "cheap outdated programming book")
    }
  }
}
package lelysi.scalashop.model

import java.util.UUID

case class ShopItem(uuid: UUID, price: Double, description: String) {
  def this(price: Double, description: String) = this(UUID.randomUUID(), price, description)
  assert(price > 0)
}
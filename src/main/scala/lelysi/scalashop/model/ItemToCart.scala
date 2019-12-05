package lelysi.scalashop.model

import java.util.UUID

case class ItemToCart(userEmail: Email, uuid: UUID)

case class ItemUuid(uuid: UUID)
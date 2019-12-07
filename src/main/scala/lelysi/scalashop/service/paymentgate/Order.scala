package lelysi.scalashop.service.paymentgate

import java.util.UUID

import lelysi.scalashop.model.{PaymentAccount, ShopItem}

case class Order(uuid: UUID, paymentAccount: PaymentAccount, items: List[ShopItem]) {
  def this(paymentAccount: PaymentAccount, items: List[ShopItem]) = this(UUID.randomUUID(), paymentAccount, items)
}

sealed trait PaymentGateResponse
case object OrderCompleted extends PaymentGateResponse
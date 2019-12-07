package lelysi.scalashop.service.paymentgate

import akka.actor.Actor
import akka.event.{Logging, LoggingAdapter}

class FakePaymentGate() extends Actor {
  implicit val log: LoggingAdapter = Logging(context.system, "main")
  override def receive: Receive = {
    case Order(uuid, _, items) =>
      val orderPrice = items.foldLeft(0.0)((acc, shopItem) => acc + shopItem.price)
      log.info(s"process order $uuid with price $orderPrice")
      sender() ! OrderCompleted
  }
}
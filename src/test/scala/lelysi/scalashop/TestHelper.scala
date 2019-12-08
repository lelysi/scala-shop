package lelysi.scalashop

import akka.testkit.TestProbe

trait TestHelper {
  def serviceMsgMap(service: TestProbe, income: AnyRef, response: AnyRef): Unit = {
    service.expectMsg(income)
    service.reply(response)
  }
}

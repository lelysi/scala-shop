package lelysi.scalashop.unit

import lelysi.scalashop.model.{Email, PaymentAccount, User}
import org.scalatest._

class UserSpec extends WordSpec {

  "User" should {
    "throw an exception" in {
      intercept[IllegalArgumentException] {
        User(Email("thisIsIncorrectEmail"), "lalala", PaymentAccount("abcde"))
      }
    }

    "be ok" in {
      User(Email("example@example.com"), "lalala", PaymentAccount("abcde"))
    }
  }
}
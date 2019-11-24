package lelysi.scalashop.unit

import lelysi.scalashop.model.User
import org.scalatest._

class UserSpec extends WordSpec {

  "User" should {
    "throw an exception" in {
      intercept[IllegalArgumentException] {
        User("thisIsIncorrectEmail", "lalala")
      }
    }

    "be ok" in {
      User("example@example.com", "lalala")
    }
  }
}
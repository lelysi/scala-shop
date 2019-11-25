package lelysi.scalashop.unit

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import lelysi.scalashop.model.User
import lelysi.scalashop.service.UserService
import lelysi.scalashop.service.UserService.{EmailAlreadyUsed, RegisterUser, UserRegistered}
import org.scalatest.WordSpecLike

class UserServiceSpec extends TestKit(ActorSystem("test-spec"))
  with WordSpecLike
  with ImplicitSender {

  "User Service" should {
    val userService = system.actorOf(UserService.props)
    "send user registered message back" in {
      userService ! RegisterUser(User("example@example.com", "pass"))
      expectMsg(UserRegistered())
    }
    "send already exists message on duplication" in {
      userService ! RegisterUser(User("example@example.com", "pass"))
      expectMsg(EmailAlreadyUsed())
    }
  }
}
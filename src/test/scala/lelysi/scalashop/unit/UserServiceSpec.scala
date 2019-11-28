package lelysi.scalashop.unit

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import lelysi.scalashop.StopSystemAfterAll
import lelysi.scalashop.model.User
import lelysi.scalashop.service.UserService
import lelysi.scalashop.service.UserService.{AuthUser, EmailAlreadyUsed, RegisterUser, UserFound, UserRegistered, UserUnknown}
import org.scalatest.WordSpecLike

class UserServiceSpec extends TestKit(ActorSystem("test-spec"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll {

  "User Service" should {
    val userService = system.actorOf(Props[UserService])
    "send user registered message back" in {
      userService ! RegisterUser(User("example@example.com", "pass"))
      expectMsg(UserRegistered)
    }
    "send already exists message on duplication" in {
      userService ! RegisterUser(User("example@example.com", "pass"))
      expectMsg(EmailAlreadyUsed)
    }
    "send UserFound if user in repository" in {
      userService ! AuthUser(User("example@example.com", "pass"))
      expectMsg(UserFound)
    }
    "send UserUnknown if user not in repository" in {
      userService ! AuthUser(User("example2@example.com", "pass"))
      expectMsg(UserUnknown)
    }
  }
}
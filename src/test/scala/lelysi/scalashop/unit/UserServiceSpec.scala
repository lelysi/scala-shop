package lelysi.scalashop.unit

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.github.t3hnar.bcrypt._
import lelysi.scalashop.StopSystemAfterAll
import lelysi.scalashop.model.{Email, User, UserLogin}
import lelysi.scalashop.service.UserService
import lelysi.scalashop.service.UserService.{AuthUser, EmailAlreadyUsed, IncorrectPassword, RegisterUser, UserFound, UserRegistered, UserUnknown}
import org.scalatest.WordSpecLike

class UserServiceSpec extends TestKit(ActorSystem("test-spec"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll {

  "User Service" should {
    val userService = system.actorOf(Props[UserService])
    "send user registered message back" in {
      userService ! RegisterUser(User(Email("example@example.com"), "pass".bcryptSafe(generateSalt).get))
      expectMsg(UserRegistered)
    }
    "send already exists message on duplication" in {
      userService ! RegisterUser(User(Email("example@example.com"), "pass".bcryptSafe(generateSalt).get))
      expectMsg(EmailAlreadyUsed)
    }
    "send UserUnknown if user not in repository" in {
      userService ! AuthUser(UserLogin(Email("example@example.com"), "pass"))
      expectMsg(UserFound)
    }
    "send UserFound if user in repository" in {
      userService ! AuthUser(UserLogin(Email("example2@example.com"), "pass"))
      expectMsg(UserUnknown)
    }
    "send IncorrectPassword if pass is incorrect" in {
      userService ! AuthUser(UserLogin(Email("example@example.com"), "pass2"))
      expectMsg(IncorrectPassword)
    }
  }
}
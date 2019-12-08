package lelysi.scalashop.unit.service

import akka.actor.{ActorRef, Props}
import com.github.t3hnar.bcrypt._
import lelysi.scalashop.model.{Email, PaymentAccount, User, UserLogin}
import lelysi.scalashop.service.UserService
import lelysi.scalashop.service.UserService._
import lelysi.scalashop.unit.UnitServiceSpec

final class UserServiceSpec extends UnitServiceSpec {

  lazy val userService: ActorRef = system.actorOf(Props[UserService])
  lazy val correctPass = "pass"
  lazy val wrongEmail = Email("noresponse@example.com")
  lazy val user = User(correctEmail, correctPass.bcryptSafe(generateSalt).get, PaymentAccount("abcde"))

  "User Service" should {
    "send user registered message on new email" in {
      userService ! RegisterUser(user)
      expectMsg(UserRegistered)
    }

    "send already exists message on duplication" in {
      userService ! RegisterUser(user)
      expectMsg(EmailAlreadyUsed)
    }

    "send UserUnknown if user not in repository" in {
      userService ! AuthUser(UserLogin(correctEmail, correctPass))
      expectMsg(UserFound(user))
    }

    "send UserFound if user in repository" in {
      userService ! AuthUser(UserLogin(wrongEmail, correctPass))
      expectMsg(UserUnknown)
    }

    "send IncorrectPassword if pass is incorrect" in {
      userService ! AuthUser(UserLogin(correctEmail, "pass2"))
      expectMsg(IncorrectPassword)
    }

    "send UserFound on correct email" in {
      userService ! FindUser(correctEmail)
      expectMsg(UserFound(user))
    }

    "send UserUnknown on incorrect email" in {
      userService ! FindUser(wrongEmail)
      expectMsg(UserUnknown)
    }
  }
}
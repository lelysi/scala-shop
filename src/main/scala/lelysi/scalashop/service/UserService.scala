package lelysi.scalashop.service

import akka.actor.Actor
import lelysi.scalashop.model.{Email, User, UserLogin}
import lelysi.scalashop.service.UserService.{AuthUser, EmailAlreadyUsed, IncorrectPassword, RegisterUser, UserFound, UserRegistered, UserUnknown}
import com.github.t3hnar.bcrypt._
import scala.collection.mutable

object UserService {
  case class RegisterUser(user: User)
  case class AuthUser(user: UserLogin)

  sealed trait UserServiceResponse

  sealed trait UserRegistrationResponse extends UserServiceResponse
  case object UserRegistered extends UserRegistrationResponse
  case object EmailAlreadyUsed extends UserRegistrationResponse

  sealed trait UserAuthenticationResponse
  case object UserFound extends UserAuthenticationResponse
  case object UserUnknown extends UserAuthenticationResponse
  case object IncorrectPassword extends UserAuthenticationResponse
}

class UserService extends Actor {
  private val userRepository: mutable.Set[User] = mutable.Set()

  def findElemByEmail(e: Email): Option[User] = {
    userRepository.find(x => x.email.email == e.email)
  }

  override def receive: Receive = {
    case RegisterUser(user) =>
      findElemByEmail(user.email) match {
        case None =>
          userRepository.add(user)
          sender() ! UserRegistered
        case _ =>
          sender() ! EmailAlreadyUsed
      }

    case AuthUser(userLogin) =>
      findElemByEmail(userLogin.email) match {
        case None => sender() ! UserUnknown
        case Some(user) =>
          if (userLogin.password.isBcrypted(user.hashedPassword))
            sender() ! UserFound
          else sender() ! IncorrectPassword
      }
  }
}
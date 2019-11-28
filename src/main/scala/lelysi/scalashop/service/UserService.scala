package lelysi.scalashop.service

import akka.actor.Actor
import lelysi.scalashop.model.User
import lelysi.scalashop.service.UserService.{AuthUser, EmailAlreadyUsed, RegisterUser, UserFound, UserRegistered, UserUnknown}

import scala.collection.mutable

object UserService {
  case class RegisterUser(user: User)
  case class AuthUser(user: User)

  sealed trait UserServiceResponse

  sealed trait UserRegistrationResponse extends UserServiceResponse
  case object UserRegistered extends UserRegistrationResponse
  case object EmailAlreadyUsed extends UserRegistrationResponse

  sealed trait UserAuthenticationResponse
  case object UserFound extends UserAuthenticationResponse
  case object UserUnknown extends UserAuthenticationResponse
}

class UserService extends Actor {
  private val userRepository: mutable.Set[User] = mutable.Set()

  override def receive: Receive = {
    case RegisterUser(user) =>
      def findElemByEmail(user: User): Option[User] = userRepository.find(x => x.email == user.email)
      findElemByEmail(user) match {
        case None =>
          userRepository.add(user)
          sender() ! UserRegistered
        case _ =>
          sender() ! EmailAlreadyUsed
      }

    case AuthUser(user) =>
      if (userRepository.contains(user)) {
        sender() ! UserFound
      } else {
        sender() ! UserUnknown
      }
  }
}
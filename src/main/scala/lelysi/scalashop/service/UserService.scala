package lelysi.scalashop.service

import akka.actor.Actor
import lelysi.scalashop.model.User
import lelysi.scalashop.service.UserService.{EmailAlreadyUsed, RegisterUser, UserRegistered}
import scala.collection.mutable

object UserService {
  case class RegisterUser(user: User)

  sealed trait UserServiceResponse
  case object UserRegistered extends UserServiceResponse
  case object EmailAlreadyUsed extends UserServiceResponse
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
  }
}
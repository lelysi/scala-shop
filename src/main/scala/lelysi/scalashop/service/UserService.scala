package lelysi.scalashop.service

import akka.actor.{Actor, Props}
import lelysi.scalashop.model.User
import lelysi.scalashop.service.UserService.{EmailAlreadyUsed, RegisterUser, UserRegistered}
import scala.collection.mutable

object UserService {
  def props: Props = Props(new UserService)

  case class RegisterUser(user: User)

  sealed trait UserServiceResponse
  case class UserRegistered() extends UserServiceResponse
  case class EmailAlreadyUsed() extends UserServiceResponse
}

class UserService extends Actor {
  private val userRepository: mutable.Set[User] = mutable.Set()

  override def receive: Receive = {
    case RegisterUser(user) =>
      def findElemByEmail(user: User): Option[User] = userRepository.find(x => x.email == user.email)
      findElemByEmail(user) match {
        case None =>
          userRepository.add(user)
          sender() ! UserRegistered()
        case _ =>
          sender() ! EmailAlreadyUsed()
      }
  }
}
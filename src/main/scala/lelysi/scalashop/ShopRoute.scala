package lelysi.scalashop

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, post}
import akka.http.scaladsl.server.Route
import model.User
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import akka.http.scaladsl.server.Directives._
import lelysi.scalashop.service.UserService
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.pattern.ask
import lelysi.scalashop.service.UserService.{EmailAlreadyUsed, RegisterUser, UserRegistered, UserServiceResponse}
import scala.concurrent.duration._
import scala.concurrent.Future

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat2(User)
}

trait ShopRoute {
  this: JsonSupport =>

  val userRegistration: Route =
    path("registration") {
      post {
        entity(as[User]) { user =>
          onSuccess(registerUser(user)) {
            case UserRegistered() => complete(s"New user with email ${user.email} was registered")
            case EmailAlreadyUsed() => complete(BadRequest, s"user already exists!")
            case _ => complete(BadRequest, s"registration failed")
          }
        }
      }
    }

  val userService: ActorRef

  def registerUser(user: User): Future[UserServiceResponse] =
    userService.ask(RegisterUser(user))(3.seconds).mapTo[UserServiceResponse]
}

class ShopApi(system: ActorSystem) extends ShopRoute with JsonSupport {
  lazy val userService: ActorRef = system.actorOf(UserService.props)
}
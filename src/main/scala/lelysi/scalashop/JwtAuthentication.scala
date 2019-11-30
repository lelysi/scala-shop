package lelysi.scalashop

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.unmarshalling.Unmarshaller
import com.emarsys.jwt.akka.http.{JwtAuthentication, JwtConfig}
import com.typesafe.config.ConfigFactory

object JwtAuthentication extends JwtAuthentication {
  case class Token(data: String)

  override val jwtConfig: JwtConfig = new JwtConfig(ConfigFactory.load())

  def auth(): Directive1[Token] = jwtAuthenticate(Unmarshaller.strict(x => Token(x)));
}
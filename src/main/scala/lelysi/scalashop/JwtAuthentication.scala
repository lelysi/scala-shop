package lelysi.scalashop

import com.typesafe.config.{Config, ConfigFactory}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import scala.util.Try

object JwtAuthentication {
  val config: Config = ConfigFactory.load()
  val secret: String = config.getString("jwt.secret")

  def getToken(claim: String): String = Jwt.encode(claim, secret, JwtAlgorithm.HS256)

  def getDecodedClaim(tokenString: String): Try[JwtClaim] = Jwt.decode(tokenString, secret, Seq(JwtAlgorithm.HS256))
}
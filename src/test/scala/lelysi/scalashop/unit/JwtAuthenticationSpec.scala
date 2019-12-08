package lelysi.scalashop.unit

import com.typesafe.config.{Config, ConfigFactory}
import lelysi.scalashop.JwtAuthentication
import org.scalatest.WordSpecLike

final class JwtAuthenticationSpec extends WordSpecLike {
  implicit val config: Config = ConfigFactory.load()
  val jwt = new JwtAuthentication(config)
  val claim = "any_string"
  val token: String = jwt.getToken(claim)
  val wrongToken: String = jwt.getToken("different-string")

  "JWT auth" should {
    "validate jwt token" in {
      assert(jwt.getDecodedClaim(token).get.content == claim)
      assert(jwt.getDecodedClaim("another_string").isFailure)
      assert(jwt.getDecodedClaim(wrongToken).get.content != claim)
    }
  }
}
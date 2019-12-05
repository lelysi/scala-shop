package lelysi.scalashop.unit

import lelysi.scalashop.JwtAuthentication
import org.scalatest.WordSpecLike

class JwtAuthenticationSpec extends WordSpecLike {
  val claim = "any_string"
  val token: String = JwtAuthentication.getToken(claim)
  val wrongToken: String = JwtAuthentication.getToken("different-string")
  "JWT auth" should {
    "validate jwt token" in {
      assert(JwtAuthentication.getDecodedClaim(token).get.content == claim)
      assert(JwtAuthentication.getDecodedClaim("another_string").isFailure)
      assert(JwtAuthentication.getDecodedClaim(wrongToken).get.content != claim)
    }
  }
}
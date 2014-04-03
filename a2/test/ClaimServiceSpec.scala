import org.joda.time.DateTime
import org.specs2.mutable.{Tags, Specification}
import services.ClaimsService
import services.mock.MockClaimsService

class ClaimServiceSpec extends Specification with Tags {
  "Claim Service" should {
    "return claims successfully for specified date" in {
      val date = new DateTime
      val service = getEndpoint
      val claims = service claimsByDate date
      claims must not(beEmpty)
      claims.get.value.size must beGreaterThan(0)

      for (claimDateTime <- claims.get.value) yield {
        (claimDateTime \ "claimDateTime").as[DateTime].toLocalDate mustEqual date.toLocalDate
      }
    }
  }

  def getEndpoint(): ClaimsService = MockClaimsService()

}

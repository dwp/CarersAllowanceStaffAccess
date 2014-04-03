import org.joda.time.DateTime
import org.specs2.mutable.{Tags, Specification}
import play.api.libs.json.JsArray
import services.ClaimsService
import services.mock.{ClaimsServiceStub}
import services.mocks.MockErrorClaimsService

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

    "must return None when something unexpected occurred" in {
      val date = new DateTime
      val service = getErrorEndpoint
      val claims = service claimsByDate date
      claims must beEmpty
    }

    "must handle an empty list when no claims for a given date" in {
      val date = new DateTime
      val service = getEmptyEndpoint
      val claims = service claimsByDate date
      claims must not(beEmpty)
      claims.get.value.size mustEqual 0
    }
  }

  def getEndpoint(): ClaimsService = ClaimsServiceStub()
  def getErrorEndpoint(): ClaimsService = MockErrorClaimsService(None)
  def getEmptyEndpoint(): ClaimsService = MockErrorClaimsService(Some(JsArray()))

}

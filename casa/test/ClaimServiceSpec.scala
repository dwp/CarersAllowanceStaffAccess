import org.joda.time.format.DateTimeFormat
import org.joda.time.{LocalDate, DateTime}
import org.specs2.mutable.{Tags, Specification}
import play.api.libs.json.JsArray
import services.ClaimsService
import services.mock.{ClaimsServiceStub}
import services.mocks.MockErrorClaimsService

class ClaimServiceSpec extends Specification with Tags {
  "Claim Service" should {
    "return claims successfully for specified date" in {
      val date = new LocalDate
      val service = getEndpoint()
      val claims = service claims date
      claims must not(beEmpty)
      claims.get.value.size must beGreaterThan(0)

      for (claimDateTime <- claims.get.value) yield {
        (claimDateTime \ "claimDateTime").as[DateTime].toLocalDate mustEqual date
      }
    }

    "must return None when something unexpected occurred" in {
      val date = new LocalDate
      val service = getErrorEndpoint
      val claims = service claims date
      claims must beEmpty
    }

    "must handle an empty list when no claims for a given date" in {
      val date = new LocalDate
      val service = getEmptyEndpoint
      val claims = service claims date
      claims must not(beEmpty)
      claims.get.value.size mustEqual 0
    }

    "return only completed claims successfully for specified date when 'completed' status specified" in {
      val date = new LocalDate
      val service = getEndpoint
      val claims = service.claimsFiltered(date, "completed")
      claims must not(beEmpty)
      claims.get.value.size must beGreaterThan(0)

      for (claim <- claims.get.value) yield {
        (claim \ "claimDateTime").as[DateTime].toLocalDate mustEqual date
        (claim \ "status").as[String] mustEqual "completed"
      }
    }

    "return a full claim when a claim is present" in {
      val transactionId = "20140101002"
      val service = getEndpoint
      val claim = service.fullClaim(transactionId)
      claim must not(beEmpty)
      (claim.get \ "transactionId").as[String] mustEqual(transactionId)
    }

    "must handle an empty response when claim is not present" in {
      val transactionId = "this-won't-be-found"
      val service = getErrorEndpoint()
      val claim = service.fullClaim(transactionId)
      claim must beEmpty
    }

    "must update claim status when not in status to be updated" in {
      val newStatus = "completed"
      val transactionId = "20140102070"
      val service = getEndpoint
      service.updateClaim(transactionId, newStatus) mustEqual(true)

      val claim = service.fullClaim(transactionId)
      claim must not(beEmpty)
      (claim.get \ "transactionId").as[String] mustEqual(transactionId)
      (claim.get \ "status").as[String] mustEqual(newStatus)
    }

    "must NOT update claim status if claim already in new status" in {
      val newStatus = "completed"
      val transactionId = "20140101002"
      val service = getErrorEndpoint
      service.updateClaim(transactionId, newStatus) mustEqual(false)
    }

    "must return number of claims in work queue for each day" in {
      val service = getEndpoint()
      val claimNumber = service.claimNumbersFiltered("viewed","received")
      val today = DateTimeFormat.forPattern("ddMMyyyy").print(new DateTime())
      (claimNumber \ today).as[Int] must be_>(0)
    }

    "must return 0 claims in work queue for today" in {
      import utils.JsValueWrapper.improveJsValue
      val service = getEndpoint()
      val claims = service.claims(new LocalDate())
      claims.get.value.foreach(jsvalue => service.updateClaim(jsvalue.p.transactionId.asString,"completed"))

      val claimNumber = service.claimNumbersFiltered("viewed","received")
      val today = DateTimeFormat.forPattern("ddMMyyyy").print(new DateTime())
      (claimNumber \ today).as[Int] mustEqual 0

    }
  }

  def getEndpoint(): ClaimsService = ClaimsServiceStub()
  def getErrorEndpoint(): ClaimsService = MockErrorClaimsService(None)
  def getEmptyEndpoint(): ClaimsService = MockErrorClaimsService(Some(JsArray()))

}

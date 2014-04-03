package services.mock

import services.ClaimsService
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.functional.syntax._

class MockClaimsService extends ClaimsService {
  override def claimsByDate(date: DateTime): Option[JsArray] = {
    Some(Json.toJson(ClaimSummary.list).asInstanceOf[JsArray])
  }
}

object MockClaimsService {
 def apply() = new MockClaimsService
}

case class ClaimSummary(nino: String, forename: String, surname: String, claimDateTime: DateTime)

object ClaimSummary {
  val list: List[ClaimSummary] = {
    List(
      ClaimSummary("some nino 1", "some forename 1", "some surname 1", new DateTime()),
      ClaimSummary("some nino 2", "some forename 2", "some surname 2", new DateTime())
    )
  }

  implicit val claimSummary: Writes[ClaimSummary] = (
    (JsPath \ "nino").write[String] and
      (JsPath \ "forename").write[String] and
      (JsPath \ "surname").write[String] and
      (JsPath \ "claimDateTime").write[DateTime]
    )(unlift(ClaimSummary.unapply))

}
package services.mock

import services.ClaimsService
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.JsArray
import scala.Some

class ClaimsServiceStub extends ClaimsService {
  override def claims(date: DateTime) = {
    Some(Json.toJson(ClaimSummary.list).asInstanceOf[JsArray])
  }

  override def claimsFiltered(date: DateTime, status: String) = {
    Some(Json.toJson(ClaimSummary.list).asInstanceOf[JsArray])
  }
}

object ClaimsServiceStub {
 def apply() = new ClaimsServiceStub
}

case class ClaimSummary(nino: String, forename: String, surname: String, claimDateTime: DateTime, status: String)

object ClaimSummary {
  val list: List[ClaimSummary] = {
    List(
      ClaimSummary("some nino 1", "some forename 1", "some surname 1", new DateTime(), "completed"),
      ClaimSummary("some nino 2", "some forename 2", "some surname 2", new DateTime(), "completed")
    )
  }

  implicit val claimSummary: Writes[ClaimSummary] = (
    (JsPath \ "nino").write[String] and
      (JsPath \ "forename").write[String] and
      (JsPath \ "surname").write[String] and
      (JsPath \ "claimDateTime").write[DateTime] and
      (JsPath \ "status").write[String]
    )(unlift(ClaimSummary.unapply))

}
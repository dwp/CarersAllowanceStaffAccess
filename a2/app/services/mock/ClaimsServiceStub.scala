package services.mock

import services.ClaimsService
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.JsArray
import scala.Some

class ClaimsServiceStub extends ClaimsService {
  var listOfClaims = ClaimSummary.list

  override def claims(date: DateTime) = {
    Some(Json.toJson(listOfClaims).asInstanceOf[JsArray])
  }

  override def claimsFiltered(date: DateTime, status: String) = {
    Some(Json.toJson(listOfClaims.filter{_.status == status}).asInstanceOf[JsArray])
  }

  override def fullClaim(transactionId: String) = {
    Some(Json.toJson(listOfClaims.filter{_.transactionId == transactionId}.head))
  }

  override def updateClaim(transactionId: String, newStatus: String): Boolean = {
    val claim = listOfClaims.filter{_.transactionId == transactionId}.headOption
    claim match {
      case Some(claimFound) if (claimFound.status != newStatus) =>
        val updatedClaim = claimFound.copy(status = newStatus)
        listOfClaims = listOfClaims.filterNot{_.transactionId == transactionId}.+:(updatedClaim)
        true
      case _ => false
    }
  }
}

object ClaimsServiceStub {
 def apply() = new ClaimsServiceStub
}

case class ClaimSummary(transactionId: String, nino: String, forename: String, surname: String, claimDateTime: DateTime, status: String)

object ClaimSummary {
  val list: List[ClaimSummary] = {
    List(
      ClaimSummary("20140101001","AB111111D", "some forename 1", "some surname 1", new DateTime(), "received"),
      ClaimSummary("20140101002","AB123456D", "some forename 2", "some surname 2", new DateTime(), "completed")
    )
  }

  implicit val claimSummary: Writes[ClaimSummary] = (
    (JsPath \ "transactionId").write[String] and
    (JsPath \ "nino").write[String] and
    (JsPath \ "forename").write[String] and
    (JsPath \ "surname").write[String] and
    (JsPath \ "claimDateTime").write[DateTime] and
    (JsPath \ "status").write[String]
  )(unlift(ClaimSummary.unapply))

}
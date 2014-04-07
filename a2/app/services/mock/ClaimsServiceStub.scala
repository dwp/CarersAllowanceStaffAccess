package services.mock

import services.ClaimsService
import org.joda.time.{LocalTime, LocalDate, DateTime}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.JsArray
import scala.Some
import scala.util.Random
import controllers.Application

class ClaimsServiceStub extends ClaimsService {
  var listOfClaims = ClaimSummary.list

  override def claims(date: LocalDate) = {
    Some(Json.toJson(listOfClaims.filter{_.claimDateTime.toLocalDate == date}).asInstanceOf[JsArray])
  }

  override def claimsFiltered(date: LocalDate, status: String) = {
    Some(Json.toJson(listOfClaims.filter{_.status == status}.filter{_.claimDateTime.toLocalDate == date}).asInstanceOf[JsArray])
  }

  override def fullClaim(transactionId: String) = {
    Some(Json.toJson(listOfClaims.filter{_.transactionId == transactionId}.head))
  }

  override def updateClaim(transactionId: String, newStatus: String): Boolean =
    listOfClaims.filter{_.transactionId == transactionId}.headOption match {
      case Some(claimFound) if claimFound.status != newStatus =>
        val updatedClaim = claimFound.copy(status = newStatus)
        listOfClaims = listOfClaims.filterNot{_.transactionId == transactionId}.+:(updatedClaim)
        true
      case _ => false
    }

}

object ClaimsServiceStub {
 def apply() = new ClaimsServiceStub
}

case class ClaimSummary(transactionId: String, nino: String, forename: String, surname: String, claimDateTime: DateTime, status: String)

object ClaimSummary {
  val daysToReport = Application.daysRec(7, new LocalDate plusDays 1, Seq())

  val list: List[ClaimSummary] =
    (for(i <- 1 to 70) yield {
      ClaimSummary(f"201401010$i%02d", f"AB${Random.nextInt(999999)}%06dD", s"name$i", s"surname$i", dayToReport, if(i == 1 || Random.nextFloat() > .5f) "received" else "completed")
    })(collection.breakOut)

  def dayToReport = {
    daysToReport(Math.abs(Random.nextInt) % daysToReport.size).toDateTime(new LocalTime())
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
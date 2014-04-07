package services.mock

import services.ClaimsService
import org.joda.time.{LocalTime, LocalDate, DateTime}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.JsArray
import scala.Some
import scala.util.Random

class ClaimsServiceStub extends ClaimsService {

  override def claims(date: LocalDate) = {
    Some(Json.toJson(listOfClaims.filter{_.claimDateTime.toLocalDate == date}.filter{_.status != "completed"}).asInstanceOf[JsArray])
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

  def specialDaysRec(n:Int,today:LocalDate, localDates: Seq[LocalDate]):Seq[LocalDate] = {
    if (n == 0) localDates
    else {
      val day = today.minusDays(1)
      specialDaysRec( n-1, day, day +: localDates)
    }
  }


  val daysToReport = specialDaysRec(7, new LocalDate plusDays 1, Seq())

  val availableStatuses = Seq("received", "viewed", "completed")

  val viewedClaimOnToday = ClaimSummary(f"20140101071", f"AB${Random.nextInt(999999)}%06dD", s"name71", s"surname71", daysToReport(daysToReport.size - 1).toDateTime(new LocalTime()), "viewed")

  def dayToReport = daysToReport(Math.abs(Random.nextInt) % daysToReport.size).toDateTime(new LocalTime())

  val randomList: List[ClaimSummary] =
    (for(i <- 1 to 70) yield {
      val statusToUse = if (i == 1) "received" else availableStatuses(Math.abs(Random.nextInt) % availableStatuses.size)
      ClaimSummary(f"201401010$i%02d", f"AB${Random.nextInt(999999)}%06dD", s"name$i", s"surname$i", dayToReport, statusToUse)
    })(collection.breakOut)

  val list =  randomList :+ viewedClaimOnToday

  var listOfClaims = list

  implicit val claimSummary: Writes[ClaimSummary] = (
    (JsPath \ "transactionId").write[String] and
      (JsPath \ "nino").write[String] and
      (JsPath \ "forename").write[String] and
      (JsPath \ "surname").write[String] and
      (JsPath \ "claimDateTime").write[DateTime] and
      (JsPath \ "status").write[String]
    )(unlift(ClaimSummary.unapply))
}

object ClaimsServiceStub {
 def apply() = new ClaimsServiceStub
}

case class ClaimSummary(transactionId: String, nino: String, forename: String, surname: String, claimDateTime: DateTime, status: String)
package controllers

import play.api.mvc._
import org.joda.time.{DateTime, LocalDate}
import services.ClaimService
import org.joda.time.format.DateTimeFormat
import play.api.data._
import play.api.data.Forms._
import play.api.templates.Html
import play.api.libs.json.JsArray
import utils.JsValueWrapper.improveJsValue
import scala.language.implicitConversions
import play.api.Logger

class Application extends Controller with Secured {

  this: ClaimService =>

  val defaultStatus = "atom"

  def index = IsAuthenticated { implicit username => implicit request =>
    val today = new LocalDate
    val claimNumbers = claimNumbersFiltered("received", "viewed")
    Ok(views.html.claimsList(today, defaultStatus, sortByDateTime(claimsFilteredBySurname(today, defaultStatus)), claimNumbers))
  }

  def sortByClaimTypeDateTime(data: Option[JsArray]): Option[JsArray] = {
    data match {
      case Some(data) =>
        Some(JsArray(
          data.value.seq.sortWith(_.p.claimDateTime.asLong < _.p.claimDateTime.asLong)
            .sortWith(_.p.claimType.asType < _.p.claimType.asType)
        ))
      case _ => data
    }
  }

  def sortByDateTime(data: Option[JsArray]): Option[JsArray] = {
    data match {
      case Some(data) =>
        Some(JsArray(
          data.value.seq.sortWith(_.p.claimDateTime.asLong < _.p.claimDateTime.asLong)
        ))
      case _ => data
    }
  }

  def claimsForDateFilteredBySurname(date: String, sortBy: String) = IsAuthenticated { implicit username => implicit request =>
    val localDate = DateTimeFormat.forPattern("ddMMyyyy").parseLocalDate(date)

    val claims = claimsFilteredBySurname(localDate, sortBy)
    val claimNumbers = claimNumbersFiltered("received", "viewed")

    Ok(views.html.claimsList(localDate, sortBy, sortByDateTime(claims), claimNumbers))
  }

  def circsForDateFiltered(date: String) = IsAuthenticated { implicit username => implicit request =>
    val localDate = DateTimeFormat.forPattern("ddMMyyyy").parseLocalDate(date)
    val circs = getCircs(localDate)
    val claimNumbers = claimNumbersFiltered("received", "viewed")
    Ok(views.html.claimsList(localDate, "circs", sortByDateTime(circs), claimNumbers))
  }

  def claimsForDate(date: String) = claimsForDateFiltered(date, "")

  def claimsForDateFiltered(date: String, status: String) = IsAuthenticated { implicit username => implicit request =>
    val localDate = DateTimeFormat.forPattern("ddMMyyyy").parseLocalDate(date)
    val claims = if (status.isEmpty) getClaims(localDate) else claimsFiltered(localDate, status)
    val claimNumbers = claimNumbersFiltered("received", "viewed")
    Ok(views.html.claimsList(localDate, status, sortByClaimTypeDateTime(claims), claimNumbers))
  }

  case class ClaimsToComplete(completedCheckboxes: List[String])

  val form = Form(
    mapping(
      "completedCheckboxes" -> list(text)
    )(ClaimsToComplete.apply)(ClaimsToComplete.unapply)
  )

  def complete(currentDate: String) = IsAuthenticated { implicit username => implicit request =>

    val redirect = Redirect(routes.Application.claimsForDateFilteredBySurname(currentDate, defaultStatus))

    form.bindFromRequest.fold(
      errors => redirect
      , claimsToComplete => {
        for (transId <- claimsToComplete.completedCheckboxes) {
          updateClaim(transId, "completed")
        }
        redirect
      }
    )

  }

  def renderClaim(transactionId: String) = IsAuthenticated { implicit username => implicit request =>
      buildClaimHtml(transactionId) match {
        case Some(renderedClaim) => Ok(Html(renderedClaim))
        case _ => Ok(views.html.common.error("/", "Error while rendering claim."))
      }
  }

  def export() = IsAuthenticated { implicit username => implicit request =>
    Logger.info("OldClaims:"+getOldClaims.toString)
    Ok(views.html.export(getOldClaims))
  }

  def csvExport() = IsAuthenticated { implicit username => implicit request =>
    val stringValue = getOldClaims match {
      case Some(s) => s.value.map(_.as[JsArray].value.mkString(",")).mkString("\n")
      case None => ""
    }

    val fileName = s"exports${DateTimeFormat.forPattern("dd-MM-yyyy").print(new DateTime)}.csv"

    Ok(stringValue).as("text/csv").withHeaders("content-disposition" -> s"attachment; filename='$fileName'")
  }

  def purge() = IsAuthenticated { implicit username => implicit request =>
    purgeOldClaims()
    Redirect(routes.Application.export())
  }
}

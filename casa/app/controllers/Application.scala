package controllers

import play.api.mvc._
import org.joda.time.{DateTime, LocalDate}
import services.ClaimServiceComponent
import org.joda.time.format.DateTimeFormat
import play.api.data._
import play.api.data.Forms._
import play.api.templates.Html
import play.api.libs.json.JsArray
import utils.JsValueWrapper.improveJsValue
import scala.language.implicitConversions

object Application extends Controller with ClaimServiceComponent with Secured {


  def index = IsAuthenticated { implicit username => implicit request =>
      val today = new LocalDate
      Ok(views.html.claimsList(today,"atom", sortByDateTime(claimService.claimsFilteredBySurname(today, "atom"))))
  }

  def sortByClaimTypeDateTime (data : Option[JsArray]):Option[JsArray] = {
    data match {
      case Some(data) =>
        Some(JsArray(
          data.value.seq.sortWith(_.p.claimDateTime.asLong < _.p.claimDateTime.asLong)
                        .sortWith(_.p.claimType.asType < _.p.claimType.asType)
        ))
      case _ => data
    }
  }

  def sortByDateTime (data : Option[JsArray]):Option[JsArray] = {
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

    val claims = claimService.claimsFilteredBySurname(localDate, sortBy)

    Ok(views.html.claimsList(localDate,sortBy, sortByDateTime(claims)))
  }

  def circsForDateFiltered(date: String) = IsAuthenticated { implicit username => implicit request =>
    val localDate = DateTimeFormat.forPattern("ddMMyyyy").parseLocalDate(date)
    val circs = claimService.circs(localDate)
    Ok(views.html.claimsList(localDate,"circs", sortByDateTime(circs)))
  }

  def claimsForDate(date: String) = claimsForDateFiltered(date,"")

  def claimsForDateFiltered(date: String, status: String) = IsAuthenticated { implicit username => implicit request =>
    val localDate = DateTimeFormat.forPattern("ddMMyyyy").parseLocalDate(date)
    val claims = if (status.isEmpty) claimService.claims(localDate) else claimService.claimsFiltered(localDate, status)
    Ok(views.html.claimsList(localDate,status, sortByClaimTypeDateTime(claims)))
  }

  case class ClaimsToComplete(completedCheckboxes:List[String])

  val form = Form(
    mapping(
      "completedCheckboxes" -> list(text)
    )(ClaimsToComplete.apply)(ClaimsToComplete.unapply)
  )

  def complete(currentDate:String) = IsAuthenticated { implicit username => implicit request =>

    val redirect = Redirect(routes.Application.claimsForDateFilteredBySurname(currentDate, "atom"))

    form.bindFromRequest.fold(
      errors => redirect
      ,claimsToComplete => {
        for(transId <- claimsToComplete.completedCheckboxes){
          claimService.updateClaim(transId,"completed")
        }
        redirect
      }
    )

  }

  def renderClaim(transactionId:String) = IsAuthenticated { implicit username => implicit request =>
    claimService.renderClaim(transactionId) match {
      case Some(renderedClaim) => Ok(Html(renderedClaim))
      case _ => BadRequest
    }
  }

  def export() = IsAuthenticated { implicit username => implicit request =>
    Ok(views.html.export(claimService.export()))
  }

  def csvExport() = IsAuthenticated { implicit username => implicit request =>
    val stringValue = claimService.export() match {
      case Some(s) => s.value.map(_.as[JsArray].value.mkString(",")).mkString("\n")
      case None => ""
    }

    val fileName = s"exports${DateTimeFormat.forPattern("dd-MM-yyyy").print(new DateTime)}.csv"

    Ok(stringValue).as("text/csv").withHeaders("content-disposition"->s"attachment; filename='$fileName'")
  }

  def purge() = IsAuthenticated { implicit username => implicit request =>
    claimService.purge()
    Redirect(routes.Application.export())
  }
}

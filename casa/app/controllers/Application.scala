package controllers

import play.api.mvc._
import org.joda.time.LocalDate
import services.ClaimServiceComponent
import org.joda.time.format.DateTimeFormat
import play.api.data._
import play.api.data.Forms._
import play.api.templates.Html
import play.api.libs.json.JsArray
import utils.JsValueWrapper.improveJsValue

object Application extends Controller with ClaimServiceComponent with Secured {

  def index = IsAuthenticated { username => implicit request =>
      val today = new LocalDate
      Ok(views.html.claimsList(today,"", sortByClaimTypeDateTime(claimService.claims(today))))
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

  def claimsForDate(date: String) = claimsForDateFiltered(date,"")

  def claimsForDateFiltered(date: String, status: String) = IsAuthenticated { username => implicit request =>
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

  def complete(currentDate:String) = IsAuthenticated { username => implicit request =>

    val redirect = Redirect(routes.Application.claimsForDate(currentDate))

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

  def renderClaim(transactionId:String) = IsAuthenticated { username => implicit request =>
    claimService.renderClaim(transactionId) match {
      case Some(renderedClaim) => Ok(Html(renderedClaim))
      case _ => BadRequest
    }
  }
}

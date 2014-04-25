package controllers

import play.api.mvc._
import org.joda.time.LocalDate
import services.ClaimServiceComponent
import org.joda.time.format.DateTimeFormat
import play.api.data._
import play.api.data.Forms._
import play.api.templates.Html
import play.api.libs.json.{JsValue, JsArray}

object Application extends Controller with ClaimServiceComponent {

  def index = Action{
    val today = new LocalDate
    Ok(views.html.claimsList(today,"", sortByClaimType(claimService.claims(today))))
  }

  def sortByClaimType (data : Option[JsArray]):Option[JsArray] = {
    data match {
      case Some(data) =>
      {
        def claimTypeOrderer = new Ordering[JsValue] {
          def compare(x:JsValue, y:JsValue) = y.\("claimType").toString().compare(x.\("claimType").toString())
        }
        Some(JsArray(data.value.sorted(claimTypeOrderer)))
      }
      case _ => data
    }
  }

  def claimsForDate(date: String) = claimsForDateFiltered(date,"")

  def claimsForDateFiltered(date: String, status: String) = Action{
    val localDate = DateTimeFormat.forPattern("ddMMyyyy").parseLocalDate(date)
    val claims = if (status.isEmpty) claimService.claims(localDate) else claimService.claimsFiltered(localDate, status)
    Ok(views.html.claimsList(localDate,status, sortByClaimType(claims)))
  }

  case class ClaimsToComplete(completedCheckboxes:List[String])

  val form = Form(
    mapping(
      "completedCheckboxes" -> list(text)
    )(ClaimsToComplete.apply)(ClaimsToComplete.unapply)
  )

  def complete(currentDate:String) = Action{ implicit request =>

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

  def renderClaim(transactionId:String) = Action {
    claimService.renderClaim(transactionId) match {
      case Some(renderedClaim) => Ok(Html(renderedClaim))
      case _ => BadRequest
    }
  }
}

package controllers

import play.api.mvc._
import org.joda.time.LocalDate
import services.{PdfServiceComponent, ClaimServiceComponent}
import org.joda.time.format.DateTimeFormat
import play.api.data._
import play.api.data.Forms._
import play.api.templates.Html

object Application extends Controller with ClaimServiceComponent with PdfServiceComponent {

  def index = Action{
    val today = new LocalDate
    Ok(views.html.claimsList(today,"", claimService.claims(today)))
  }

  def claimsForDate(date: String) = claimsForDateFiltered(date,"")

  def claimsForDateFiltered(date: String, status: String) = Action{
    val localDate = DateTimeFormat.forPattern("ddMMyyyy").parseLocalDate(date)
    val claims = if (status.isEmpty) claimService.claims(localDate) else claimService.claimsFiltered(localDate, status)
    Ok(views.html.claimsList(localDate,status, claims))
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

  def claimPdf(transactionId:String) = Action{
    claimService.updateClaim(transactionId,"viewed")
    val htmlString = pdfService.claimHtml(transactionId).replace("</body>","<script>window.onload = function(){window.opener.location.reload(false);};</script></body>")
    Ok(Html(htmlString))
  }
}


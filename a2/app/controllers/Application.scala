package controllers

import play.api.mvc._
import org.joda.time.{DateTime, LocalDate}
import scala.annotation.tailrec
import services.ClaimService
import org.joda.time.format.DateTimeFormat
import play.api.data.Form
import play.api.data._
import play.api.data.Forms._

object Application extends Controller with ClaimService{

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



  def renderClaim(transactionId:String) = Action{
    // TODO: Update claim status in future when "viewed" status kicks in
    // TODO: Get full claim details
    // TODO: Use full claim details to render
    Ok("")
  }
}


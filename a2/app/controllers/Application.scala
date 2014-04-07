package controllers

import play.api.mvc._
import org.joda.time.{DateTime, LocalDate}
import scala.annotation.tailrec
import services.ClaimService
import org.joda.time.format.DateTimeFormat

object Application extends Controller with ClaimService{

  def index = Action{
    Ok(views.html.claimsList(claimService.claims(new LocalDate())))
  }

  def claimsForDate(date: String) = Action{
    Ok(views.html.claimsList(claimService.claims(DateTimeFormat.forPattern("ddMMyyyy").parseLocalDate(date))))
  }



  def renderClaim(transactionId:String) = Action{
    // TODO: Update claim status in future when "viewed" status kicks in
    // TODO: Get full claim details
    // TODO: Use full claim details to render
    Ok("")
  }



  @tailrec
  def daysRec(n:Int,today:LocalDate, localDates: Seq[LocalDate]):Seq[LocalDate] = {
    if (n == 0) localDates
    else {
      val day = today.minusDays(1)
      daysRec( n-1, day, day +: localDates)
    }
  }
}


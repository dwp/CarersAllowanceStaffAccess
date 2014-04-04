package controllers

import play.api.mvc._
import org.joda.time.{DateTime, LocalDate}
import scala.annotation.tailrec
import services.ClaimService

object Application extends Controller with ClaimService{

  def index = Action {
    Ok(views.html.claimsList(claimService.claims(new DateTime())))
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


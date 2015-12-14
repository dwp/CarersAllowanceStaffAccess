package controllers

import javax.inject.Inject

import play.api.Logger
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc._
import org.joda.time.{DateTime, LocalDate}
import org.joda.time.format.DateTimeFormat
import play.api.data._
import play.api.data.Forms._
import play.twirl.api.Html
import play.api.libs.json.{JsObject, JsValue, JsString, JsArray}
import services.ClaimService
import scala.language.implicitConversions
import utils.{SortBy, ApplicationUtils}
import play.api.Play.current

class Application @Inject() (claimService: ClaimService) extends Controller with Secured with I18nSupport {

  val defaultStatus = "atom"

  def index = IsAuthenticated { implicit username => implicit request =>
    val today = new LocalDate
    val originTag = getOriginTag(request)
    val claimNumbers = claimService.claimNumbersFiltered(originTag, "received", "viewed")
    val countOfClaimsTabs = claimService.countOfClaimsForTabs(originTag, today)
    Ok(views.html.claimsList(today, defaultStatus, SortBy surname(claimService.claimsFilteredBySurname(originTag, today, defaultStatus)), claimNumbers, countForTabs(countOfClaimsTabs), originTag))
  }

  def claimsForDateFilteredBySurname(date: String, sortBy: String) = Action { implicit request =>
    val localDate = DateTimeFormat.forPattern("ddMMyyyy").parseLocalDate(date)
    val originTag = getOriginTag(request)
    val claims = claimService.claimsFilteredBySurname(originTag, localDate, sortBy)
    val claimNumbers = claimService.claimNumbersFiltered(originTag, "received", "viewed")
    val countOfClaimsTabs = claimService.countOfClaimsForTabs(originTag, localDate)

    Ok(views.html.claimsList(localDate, sortBy, SortBy surname(claims), claimNumbers, countForTabs(countOfClaimsTabs), originTag))
  }

  def circsForDateFiltered(date: String) = IsAuthenticated { implicit username => implicit request =>
    val localDate = DateTimeFormat.forPattern("ddMMyyyy").parseLocalDate(date)
    val originTag = getOriginTag(request)
    val circs = claimService.getCircs(originTag, localDate)
    val claimNumbers = claimService.claimNumbersFiltered(originTag, "received", "viewed")
    val countOfClaimsTabs = claimService.countOfClaimsForTabs(originTag, localDate)
    Ok(views.html.claimsList(localDate, "circs", SortBy dateTime(circs), claimNumbers, countForTabs(countOfClaimsTabs), originTag))
  }

  def claimsForDate(date: String) = claimsForDateFiltered(date, "")

  def claimsForDateFiltered(date: String, status: String) = IsAuthenticated { implicit username => implicit request =>
    val localDate = DateTimeFormat.forPattern("ddMMyyyy").parseLocalDate(date)
    val originTag = getOriginTag(request)
    val claims = if (status.isEmpty) claimService.getClaims(originTag, localDate) else claimService.claimsFiltered(originTag, localDate, status)
    val claimNumbers = claimService.claimNumbersFiltered(originTag, "received", "viewed")
    val countOfClaimsTabs = claimService.countOfClaimsForTabs(originTag, localDate)
    Ok(views.html.claimsList(localDate, status, SortBy claimTypeDateTime(claims), claimNumbers, countForTabs(countOfClaimsTabs), originTag))
  }

  private def countForTabs(countsRecieved:JsObject):JsObject = {
     (countsRecieved \ "counts").as[JsObject]
  }

  case class ClaimsToComplete(completedCheckboxes: List[String])

  val form = Form(
    mapping(
      "completedCheckboxes" -> list(text)
    )(ClaimsToComplete.apply)(ClaimsToComplete.unapply)
  )

  def complete(currentDate: String) = IsAuthenticated { implicit username => implicit request =>
    val originTag = getOriginTag(request)
    val redirect = Redirect(routes.Application.claimsForDateFilteredBySurname(currentDate, defaultStatus))

    form.bindFromRequest.fold(
      errors => redirect
      , claimsToComplete => {
        for (transId <- claimsToComplete.completedCheckboxes) {
          claimService.updateClaim(transId, "completed")
        }
        redirect
      }
    )

  }

  def renderClaim(transactionId: String) = IsAuthenticated { implicit username => implicit request =>
    val originTag = getOriginTag(request)
    claimService.buildClaimHtml(transactionId, originTag) match {
        case Some(renderedClaim) => Ok(Html(renderedClaim))
        case _ =>
          Logger.error(s"Problem rendering claim [$transactionId]")
          Ok(views.html.common.error(ApplicationUtils.startPage, "Problem rendering claim."))
      }
  }

  def export() = IsAuthenticated { implicit username => implicit request =>
    val originTag = getOriginTag(request)
    Ok(views.html.export(claimService.getOldClaims(originTag)))
  }

  def csvExport() = IsAuthenticated { implicit username => implicit request =>
    val originTag = getOriginTag(request)
    val stringValue = claimService.getOldClaims(originTag) match {
      case Some(s) =>
        //We need to parse the date time to be readable on the CSV by excel and openoffice
        val claimDateTimePos = s.value(0).as[JsArray].value.indexOf(JsString("claimDateTime"))


        JsArray(s.value.drop(1).map{a =>
          //Getting the element we have to change by position
          val elemToChange = a.as[JsArray].value.zipWithIndex.find(_._2 == claimDateTimePos)
          //As order by position is important we save element lists previous and after the element we have to change
          val prev = a.as[JsArray].value.zipWithIndex.filter(_._2 < claimDateTimePos).map(_._1)
          val after = a.as[JsArray].value.zipWithIndex.filter(_._2 > claimDateTimePos).map(_._1)

          //We parse the element into what we want
          val parsedDate = DateTimeFormat.forPattern("ddMMyyyyHHmm").parseDateTime(elemToChange.get._1.as[JsString].value)
          val modified = JsString(DateTimeFormat.forPattern("dd/MMM/yyyy HH:mm").print(parsedDate)).as[JsValue]

          //We put everything back together in the order they used to be
          JsArray(prev.:+(modified) ++: after)
          //This last bit is to put back at the start the column titles
        }.+:(s.value(0).as[JsValue])).value.map(_.as[JsArray].value.mkString(",")).mkString("\n")

      case None => ""
    }

    val fileName = s"exports${DateTimeFormat.forPattern("dd-MM-yyyy").print(new DateTime)}.csv"

    Ok(stringValue).as("text/csv").withHeaders("content-disposition" -> s"attachment; filename=$fileName")
  }

  def purge() = IsAuthenticated { implicit username => implicit request =>
    val originTag = getOriginTag(request)
    claimService.purgeOldClaims(originTag)
    Redirect(routes.Application.export())
  }

  override def messagesApi: MessagesApi = current.injector.instanceOf[MessagesApi]
}

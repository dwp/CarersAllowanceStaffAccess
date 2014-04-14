package services

import scala.concurrent.duration._
import org.joda.time.LocalDate
import play.api.libs.json._
import play.api.Play
import play.api.libs.ws.WS
import org.joda.time.format.DateTimeFormat
import play.api.http.Status
import scala.concurrent.Await
import scala.language.postfixOps
import play.api.libs.json.JsObject
import play.api.libs.json.JsArray
import scala.Some


object ClaimServiceImpl extends ClaimsService{

  val url = Play.configuration(Play.current).getString("claimsServiceUrl").getOrElse("http://localhost:9002")
  val timeout = Play.configuration(Play.current).getInt("ws.timeout").getOrElse(30).seconds

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  override def claims(date: LocalDate): Option[JsArray] = {
    val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(date)

    val futureResponse = WS.url(s"$url/claims/$dateString").get().map(response =>
      response.status match {
        case Status.OK => Some(response.json.as[JsArray])
        case Status.BAD_REQUEST => None
      }
    )

    Await.result(futureResponse,timeout)
  }

  override def claimsFiltered(date: LocalDate, status: String): Option[JsArray] = {

    val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(date)

    val futureResponse = WS.url(s"$url/claims/$dateString/$status").get().map(response =>
      response.status match {
        case Status.OK => Some(response.json.as[JsArray])
        case Status.BAD_REQUEST => None
      }
    )

    Await.result(futureResponse,timeout)
  }

  override def claimNumbersFiltered(status: String*): JsObject = Json.parse("{}").as[JsObject]

  override def updateClaim(transactionId: String, status: String): JsBoolean = {

    val futureResponse = WS.url(s"$url/claim/$transactionId/$status").put(Map.empty[String,Seq[String]]).map(response =>
      response.status match {
        case Status.OK => JsBoolean(true)
        case Status.BAD_REQUEST => JsBoolean(false)
      }
    )
    Await.result(futureResponse,timeout)
  }

  override def fullClaim(transactionId: String): Option[JsValue] = {

    val futureResponse = WS.url(s"$url/claim/$transactionId/").get().map(response =>
      response.status match {
        case Status.OK => Some(response.json)
        case Status.BAD_REQUEST => None
      }
    )
    Await.result(futureResponse,timeout)
  }


}

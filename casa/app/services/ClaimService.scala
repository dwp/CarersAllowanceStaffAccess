package services

import utils.HttpUtils.HttpMethodWrapper
import org.joda.time.LocalDate
import play.api.libs.json._
import org.joda.time.format.DateTimeFormat
import play.api.http.Status
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import play.api.libs.json.JsBoolean
import scala.Some
import scala.language.implicitConversions
import monitoring.Counters

trait ClaimService extends CasaRemoteService {

  override def getUrlPropertyName = "claimsServiceUrl"

  override def getTimeoutPropertyName = "ws.timeout"

  override def getDefaultUrl = "http://localhost:9002"

  implicit def stringGetWrapper(url: String) = new HttpMethodWrapper(url, timeout)

  def getClaims(date: LocalDate): Option[JsArray] = {
    val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(date)

    s"$url/claims/$dateString" get { response =>
      response.status match {
        case Status.OK => Some(response.json.as[JsArray])
        case Status.NOT_FOUND => None
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          None
      }
    }
  }

  def getCircs(date: LocalDate): Option[JsArray] = {
    val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(date)

    s"$url/circs/$dateString" get { response =>
      response.status match {
        case Status.OK => Some(response.json.as[JsArray])
        case Status.NOT_FOUND => None
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          None
      }
    }
  }

  def claimsFilteredBySurname(date: LocalDate, sortBy: String): Option[JsArray] = {

    val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(date)

    s"$url/claims/surname/$dateString/$sortBy" get { response =>
      response.status match {
        case Status.OK => Some(response.json.as[JsArray])
        case Status.NOT_FOUND => None
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          None
      }
    }

  }

  def claimsFiltered(date: LocalDate, status: String): Option[JsArray] = {

    val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(date)

    s"$url/claims/$dateString/$status" get { response =>
      response.status match {
        case Status.OK => Some(response.json.as[JsArray])
        case Status.NOT_FOUND => None
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          None
      }
    }

  }

  def claimNumbersFiltered(status: String*): JsObject =
    s"$url/counts/${status.mkString(",")}" get { response =>
      response.status match {
        case Status.OK => response.json.as[JsObject]
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          Json.parse("{}").as[JsObject]
      }
    }

  def updateClaim(transactionId: String, status: String): JsBoolean =
    s"$url/claim/$transactionId/$status" put { response =>
      response.status match {
        case Status.OK => new JsBoolean(true)
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          new JsBoolean(false)
      }
    } exec()

  def fullClaim(transactionId: String): Option[JsValue] =
    s"$url/claim/$transactionId/" get { response =>
      response.status match {
        case Status.OK => Some(response.json)
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          None
      }
    }

  def buildClaimHtml(transactionId: String) = {
    s"$url/render/$transactionId" get { response =>
      response.status match {
        case Status.OK =>
          val html = response.body
          Some(
            html.replace("<title></title>",s"<title>Claim PDF $transactionId</title>")
                .replace("##CHECK##","""<img src="/assets/img/yes.png" style="height:20px;"/>""")
                .replace("##CROSS##","""<img src="/assets/img/no.png" style="height:20px;"/>""")
                .replace("</body>","<script>window.onload = function(){window.opener.location.reload(false);};</script></body>")
          )
        case Status.NOT_FOUND => None
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          None
      }
    }
  }


  def getOldClaims: Option[JsArray] = {
    s"$url/export" get { response =>
      response.status match {
        case Status.OK => Some(response.json.as[JsArray])
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          None
      }
    }
  }

  def purgeOldClaims(): JsBoolean = {
    s"$url/purge" post { response =>
      response.status match {
        case Status.OK => JsBoolean(value = true)
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          JsBoolean(value = false)
      }
    } exec()
  }
}

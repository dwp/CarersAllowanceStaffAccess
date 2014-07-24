package services

import javax.xml.bind.DatatypeConverter

import com.dwp.carers.security.encryption.{NotEncryptedException, EncryptorAES}
import com.dwp.exceptions.DwpRuntimeException
import monitoring.Counters
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{JsArray, JsBoolean, JsObject, _}
import utils.HttpUtils.HttpMethodWrapper

import scala.language.implicitConversions

trait ClaimService extends CasaRemoteService {

  override def getUrlPropertyName = "claimsServiceUrl"

  override def getTimeoutPropertyName = "cs.timeout"

  override def getDefaultUrl = "http://localhost:9002"

  implicit def stringGetWrapper(url: String) = new HttpMethodWrapper(url, timeout)

  def getClaims(date: LocalDate): Option[JsArray] = {
    val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(date)

    s"$url/claims/$dateString" get { response =>
      response.status match {
        case Status.OK => Some(decryptArray(response.json.as[JsArray]))
        case Status.NOT_FOUND => 
          Logger.warn(s"Claim service did not find claims for date  ${dateString}")
          None
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
        case Status.OK => Some(decryptArray(response.json.as[JsArray]))
        case Status.NOT_FOUND => 
          Logger.warn(s"Claim service did not find coc for date ${dateString}")
          None
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
        case Status.OK => Some(decryptArray(response.json.as[JsArray]))
        case Status.NOT_FOUND => 
          Logger.warn(s"Claim service did not find claims for date ${dateString} and sort by ${sortBy}.")
          None
        
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
        case Status.OK => Some(decryptArray(response.json.as[JsArray]))
        case Status.NOT_FOUND => 
          Logger.warn(s"Claim service did not find claims for date  ${dateString} and status ${status}.")
          None
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
        case Status.BAD_REQUEST => 
          Logger.error(s"Claim service could not count claims with status ${status}")
          Counters.incrementCsSubmissionErrorStatus(response.status)
          Json.parse("{}").as[JsObject]
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          Json.parse("{}").as[JsObject]
      }
    }

  def updateClaim(transactionId: String, status: String): JsBoolean =
    s"$url/claim/$transactionId/$status" put { response =>
      response.status match {
        case Status.OK => new JsBoolean(true)
        case Status.BAD_REQUEST => 
          Logger.error(s"Claim service did not update claim transactionId [${transactionId}] and status ${status}.")
          Counters.incrementCsSubmissionErrorStatus(response.status)
          new JsBoolean(false)
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          new JsBoolean(false)
      }
    } exec()

  def fullClaim(transactionId: String): Option[JsValue] =
    s"$url/claim/$transactionId/" get { response =>
      response.status match {
        case Status.OK => Some(response.json)
        case Status.BAD_REQUEST => 
          Logger.error(s"Claim service did not find claim transactionId [${transactionId}].")
          Counters.incrementCsSubmissionErrorStatus(response.status)
          None
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
        case Status.NOT_FOUND =>
          Logger.error(s"Claim service could not build html for claim transactionId [${transactionId}].")
          None
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          None
      }
    }
  }


  def getOldClaims: Option[JsArray] = {
    s"$url/export" get { response =>
      response.status match {
        case Status.OK => Some(decryptArray(response.json.as[JsArray]))
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

  private def decryptArray(toDecrypt:JsArray):JsArray = {
    val ret = toDecrypt.value.map { jsValue =>
      JsObject(jsValue.as[JsObject].value.map { tuple =>
        if (tuple._2.isInstanceOf[JsString]){
          tuple._1 -> JsString(decryptString(tuple._2.as[JsString].value))
        }else{
          tuple
        }
      }.toSeq)
    }

    Json.toJson(ret).as[JsArray]
  }

  def decryptString(text: String) = {
    try {
      (new EncryptorAES).decrypt(DatatypeConverter.parseBase64Binary(text))
    } catch {
      case e: NotEncryptedException =>
        // Means field was not encrypted.
        text
      case e: DwpRuntimeException =>
        Logger.error("Could not decrypt node.")
        throw e
    }
  }
}

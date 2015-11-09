package services

import javax.xml.bind.DatatypeConverter

import gov.dwp.carers.security.encryption.{NotEncryptedException, EncryptorAES}
import gov.dwp.exceptions.DwpRuntimeException
import monitoring.Counters
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{JsArray, JsBoolean, JsObject, _}
import utils.JsValueWrapper.improveJsValue
import scala.language.implicitConversions


class ClaimServiceImpl extends CasaRemoteService with RenderServiceComponent with ClaimService {

  override def getUrlPropertyName = "claimsServiceUrl"

  override def getTimeoutPropertyName = "cs.timeout"

  override def getDefaultUrl = "http://localhost:9002"


  override def getClaims(date: LocalDate): Option[JsArray] = {
    val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(date)

    s"$url/claims/$dateString" get { response =>
      response.status match {
        case Status.OK => Some(decryptObjArray(response.json.as[JsArray]))
        case Status.NOT_FOUND =>
          Logger.warn(s"Claim service did not find claims for date  $dateString")
          None
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          None
      }
    }
  }

  override def getCircs(date: LocalDate): Option[JsArray] = {
    val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(date)

    s"$url/circs/$dateString" get { response =>
      response.status match {
        case Status.OK => Some(decryptObjArray(response.json.as[JsArray]))
        case Status.NOT_FOUND =>
          Logger.warn(s"Claim service did not find coc for date $dateString")
          None
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          None
      }
    }
  }

  override def claimsFilteredBySurname(date: LocalDate, sortBy: String): Option[JsArray] = {

    val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(date)

    s"$url/claims/surname/$dateString/$sortBy" get { response =>
      response.status match {
        case Status.OK => Some(decryptObjArray(response.json.as[JsArray]))
        case Status.NOT_FOUND =>
          Logger.warn(s"Claim service did not find claims for date $dateString and sort by $sortBy.")
          None
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          None
      }
    }

  }

  override def claimsFiltered(date: LocalDate, status: String): Option[JsArray] = {

    val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(date)

    s"$url/claims/$dateString/$status" get { response =>
      response.status match {
        case Status.OK => Some(decryptObjArray(response.json.as[JsArray]))
        case Status.NOT_FOUND =>
          Logger.warn(s"Claim service did not find claims for date  $dateString and status $status.")
          None
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          None
      }
    }

  }

  override def claimNumbersFiltered(status: String*): JsObject =
    s"$url/counts/${status.mkString(",")}" get { response =>
      response.status match {
        case Status.OK => response.json.as[JsObject]
        case Status.BAD_REQUEST =>
          Logger.error(s"Claim service could not count claims with status $status")
          Counters.incrementCsSubmissionErrorStatus(response.status)
          Json.parse("{}").as[JsObject]
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          Json.parse("{}").as[JsObject]
      }
    }

  override def countOfClaimsForTabs(date: LocalDate):JsObject = {
    val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(date)

    s"$url/countOfClaimsForTabs/$dateString" get { response =>
      response.status match {
        case Status.OK => response.json.as[JsObject]
        case Status.BAD_REQUEST =>
          Logger.error(s"Claim service could not get count of claims for tabs for $dateString")
          Counters.incrementCsSubmissionErrorStatus(response.status)
          Json.parse("{}").as[JsObject]
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          Json.parse("{}").as[JsObject]
      }
    }
  }


  override def updateClaim(transactionId: String, status: String): JsBoolean =
    s"$url/claim/$transactionId/$status" put { response =>
      response.status match {
        case Status.OK => new JsBoolean(true)
        case Status.BAD_REQUEST =>
          Logger.error(s"Claim service did not update claim transactionId [$transactionId] and status $status.")
          Counters.incrementCsSubmissionErrorStatus(response.status)
          new JsBoolean(false)
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          new JsBoolean(false)
      }
    } exec()

  override def fullClaim(transactionId: String): Option[JsValue] =
    s"$url/claim/$transactionId/" get { response =>
      response.status match {
        case Status.OK => Some(response.json)
        case Status.BAD_REQUEST =>
          Logger.error(s"Claim service did not find claim transactionId [$transactionId].")
          Counters.incrementCsSubmissionErrorStatus(response.status)
          None
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          None
      }
    }

  override def buildClaimHtml(transactionId: String) = {
    s"$url/claim/$transactionId" get { response =>
      response.status match {
        case Status.OK =>
          Logger.debug(s"Received claim from claim service [$transactionId].")
          val claimXml = response.body
          val html = renderService.claimHtml(claimXml.mkString).mkString
          Logger.debug(s"Received response from rendering service? ${html.nonEmpty}.")
          Some(
            html.replace("<title></title>",s"<title>Claim PDF $transactionId</title>")
              .replace("</body>","<script>window.onload = function(){window.opener.location.reload(false);};</script></body>")
          )
        case Status.NOT_FOUND =>
          Logger.error(s"Claim service could not build html for claim transactionId [$transactionId].")
          None
        case _ =>
          Logger.error(s"Failed building an html for claim [$transactionId] status ${response.status} full response: ${response.toString}")
          Counters.incrementCsSubmissionErrorStatus(response.status)
          None
      }
    }
  }


  override def getOldClaims: Option[JsArray] = {
    s"$url/export" get { response =>
      response.status match {
        case Status.OK =>
          Some(decryptArray(response.json.as[JsArray]))
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          None
      }
    }
  }

  override def purgeOldClaims(): JsBoolean = {
    s"$url/purge" post { response =>
      response.status match {
        case Status.OK => JsBoolean(value = true)
        case _ =>
          Counters.incrementCsSubmissionErrorStatus(response.status)
          JsBoolean(value = false)
      }
    } exec()
  }

  private def decryptObjArray(toDecrypt:JsArray):JsArray = {
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

  private def decryptArray(toDecrypt:JsArray):JsArray = {
    val ret = toDecrypt.value.map { jsValue =>
      JsArray(jsValue.as[JsArray].value.map { elem =>
        if (elem.isInstanceOf[JsString]){
          JsString(decryptString(elem.as[JsString].value))
        }else{
          elem
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
        Logger.error("Could not decrypt node.",e)
        throw e
      case e: IndexOutOfBoundsException =>
        // In case the text is not encrypted and has accented characters IOOBE will fire
        text
    }
  }
}

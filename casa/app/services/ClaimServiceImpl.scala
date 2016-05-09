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
import utils.RenameThread
import scala.language.implicitConversions
import app.ConfigProperties._
import utils.HttpUtils.HttpWrapper

class ClaimServiceImpl extends CasaRemoteService with RenderServiceComponent with ClaimService {

  override def getUrlPropertyName = "http://localhost:9002"

  override def getTimeoutPropertyName = "cs.timeout"

  override def getDefaultUrl = ""

  def csurl = {
    getStringProperty("claimsServiceUrl")
  }

  def cstimeout = {
    getIntProperty("cs.timeout")
  }

  def httpWrapper = new HttpWrapper

  override def getClaims(originTag: String, date: LocalDate): Option[JsArray] = {
    val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(date)
    val response = httpWrapper.get(s"$csurl/claims/$dateString/$originTag", cstimeout)
    response.getStatus match {
      case Status.OK => Some(decryptObjArray(Json.parse(response.getResponse).as[JsArray]))
      case Status.NOT_FOUND =>
        Logger.warn(s"Claim service did not find claims for date  $dateString")
        None
      case _ =>
        Counters.incrementCsSubmissionErrorStatus(response.getStatus)
        None
    }
  }

  override def getCircs(originTag: String, date: LocalDate): Option[JsArray] = {
    val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(date)
    val response = httpWrapper.get(s"$csurl/circs/$dateString/$originTag", cstimeout)
    response.getStatus match {
      case Status.OK => Some(decryptObjArray(Json.parse(response.getResponse).as[JsArray]))
      case Status.NOT_FOUND =>
        Logger.warn(s"Claim service did not find coc for date $dateString")
        None
      case _ =>
        Counters.incrementCsSubmissionErrorStatus(response.getStatus)
        None
    }
  }

  override def claimsFilteredBySurname(originTag: String, date: LocalDate, sortBy: String): Option[JsArray] = {
    val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(date)
    val response = httpWrapper.get(s"$csurl/claims/surname/$dateString/$sortBy/$originTag", cstimeout)
    response.getStatus match {
      case Status.OK => Some(decryptObjArray(Json.parse(response.getResponse).as[JsArray]))
      case Status.NOT_FOUND =>
        Logger.warn(s"Claim service did not find claims for date $dateString and sort by $sortBy.")
        None
      case _ =>
        Counters.incrementCsSubmissionErrorStatus(response.getStatus)
        None
    }
  }

  override def claimsFiltered(originTag: String, date: LocalDate, status: String): Option[JsArray] = {
    val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(date)
    val response = httpWrapper.get(s"$csurl/claims/$dateString/$status/$originTag", cstimeout)
    response.getStatus match {
      case Status.OK => Some(decryptObjArray(Json.parse(response.getResponse).as[JsArray]))
      case Status.NOT_FOUND =>
        Logger.warn(s"Claim service did not find claims for date  $dateString and status $status.")
        None
      case _ =>
        Counters.incrementCsSubmissionErrorStatus(response.getStatus)
        None
    }
  }

  override def claimNumbersFiltered(originTag: String, status: String*): JsObject = {
    val response = httpWrapper.get(s"$csurl/counts/${status.mkString(",")}/$originTag", cstimeout)
    response.getStatus match {
      case Status.OK => Json.parse(response.getResponse).as[JsObject]
      case Status.BAD_REQUEST =>
        Logger.error(s"Claim service could not count claims with status $status")
        Counters.incrementCsSubmissionErrorStatus(response.getStatus)
        Json.parse("{}").as[JsObject]
      case _ =>
        Counters.incrementCsSubmissionErrorStatus(response.getStatus)
        Json.parse("{}").as[JsObject]
    }
  }

  override def countOfClaimsForTabs(originTag: String, date: LocalDate): JsObject = {
    val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(date)
    val response = httpWrapper.get(s"$csurl/countOfClaimsForTabs/$dateString/$originTag", cstimeout)
    response.getStatus match {
      case Status.OK => Json.parse(response.getResponse).as[JsObject]
      case Status.BAD_REQUEST =>
        Logger.error(s"Claim service could not get count of claims for tabs for $dateString")
        Counters.incrementCsSubmissionErrorStatus(response.getStatus)
        Json.parse("{}").as[JsObject]
      case _ =>
        Counters.incrementCsSubmissionErrorStatus(response.getStatus)
        Json.parse("{}").as[JsObject]
    }
  }

  override def updateClaim(transactionId: String, status: String): JsBoolean = {
    val response = httpWrapper.put(s"$csurl/claim/$transactionId/$status", cstimeout)
    RenameThread.renameThreadFromTransactionId(transactionId)
    response.getStatus match {
      case Status.OK => new JsBoolean(true)
      case Status.BAD_REQUEST =>
        Logger.error(s"Claim service did not update claim transactionId [$transactionId] and status $status.")
        Counters.incrementCsSubmissionErrorStatus(response.getStatus)
        new JsBoolean(false)
      case _ =>
        Counters.incrementCsSubmissionErrorStatus(response.getStatus)
        new JsBoolean(false)
    }
  }

  override def fullClaim(transactionId: String, originTag: String): Option[JsValue] = {
    val response = httpWrapper.get(s"$csurl/claim/$transactionId/$originTag", cstimeout)
    RenameThread.renameThreadFromTransactionId(transactionId)
    response.getStatus match {
      case Status.OK => Some(Json.parse(response.getResponse))
      case Status.BAD_REQUEST =>
        Logger.error(s"Claim service did not find claim transactionId [$transactionId].")
        Counters.incrementCsSubmissionErrorStatus(response.getStatus)
        None
      case _ =>
        Counters.incrementCsSubmissionErrorStatus(response.getStatus)
        None
    }
  }

  override def buildClaimHtml(transactionId: String, originTag: String) = {
    val response = httpWrapper.get(s"$csurl/claim/$transactionId/$originTag", cstimeout)
    RenameThread.renameThreadFromTransactionId(transactionId)
    response.getStatus match {
      case Status.OK =>
        Logger.debug(s"Received claim from claim service [$transactionId].")
        val claimXml = response.getResponse
        val html = renderService.claimHtml(claimXml.mkString).mkString
        Logger.debug(s"Received response from rendering service? ${html.nonEmpty}.")
        Some(html.replace("<title></title>", s"<title>Claim PDF $transactionId</title>")
          .replace("</body>", "<script>window.onload = function(){window.opener.location.reload(false);};</script></body>"))
      case Status.NOT_FOUND =>
        Logger.error(s"Claim service could not build html for claim transactionId [$transactionId].")
        None
      case _ =>
        Logger.error(s"Failed building an html for claim [$transactionId] status ${response.getStatus} full response: ${response.toString}")
        Counters.incrementCsSubmissionErrorStatus(response.getStatus)
        None
    }
  }

  override def getOldClaims(originTag: String): Option[JsArray] = {
    val response = httpWrapper.get(s"$csurl/export/$originTag", cstimeout)
    response.getStatus match {
      case Status.OK =>
        Some(decryptArray(Json.parse(response.getResponse).as[JsArray]))
      case _ =>
        Counters.incrementCsSubmissionErrorStatus(response.getStatus)
        None
    }
  }

  override def purgeOldClaims(originTag: String): JsBoolean = {
    val response = httpWrapper.post(s"$csurl/purge/$originTag", "", cstimeout)
    response.getStatus match {
      case Status.OK => JsBoolean(value = true)
      case _ =>
        Counters.incrementCsSubmissionErrorStatus(response.getStatus)
        JsBoolean(value = false)
    }
  }

  private def decryptObjArray(toDecrypt: JsArray): JsArray = {
    val ret = toDecrypt.value.map {
      jsValue =>
        JsObject(jsValue.as[JsObject].value.map {
          tuple =>
            if (tuple._2.isInstanceOf[JsString]) {
              tuple._1 -> JsString(decryptString(tuple._2.as[JsString].value))
            } else {
              tuple
            }
        }.toSeq)
    }

    Json.toJson(ret).as[JsArray]
  }

  private def decryptArray(toDecrypt: JsArray): JsArray = {
    val ret = toDecrypt.value.map {
      jsValue =>
        JsArray(jsValue.as[JsArray].value.map {
          elem =>
            if (elem.isInstanceOf[JsString]) {
              JsString(decryptString(elem.as[JsString].value))
            } else {
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
        Logger.error("Could not decrypt node.", e)
        throw e
      case e: IndexOutOfBoundsException =>
        // In case the text is not encrypted and has accented characters IOOBE will fire
        text
    }
  }
}

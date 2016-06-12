package services

import play.api.Logger
import play.api.libs.json._
import play.api.http.Status
import utils.HttpWrapper
import scala.language.postfixOps
import scala.language.implicitConversions
import monitoring.Counters
import app.ConfigProperties._

trait AccessControlService {
  def findByUserId(userId: String): JsValue
  def getDaysToExpiration(userId: String): JsValue
}

class AccessControlServiceImpl extends CasaRemoteService with AccessControlService {
  override def getUrlPropertyName = "accessControlServiceUrl"

  override def getTimeoutPropertyName = "n/a"
  override def getDefaultUrl = "n/a"

  def findByUserId(userId: String): JsValue = {
    val timeout = getIntProperty("ac.timeout")
    val url = getStringProperty("accessControlServiceUrl") + s"/user/$userId"
    val httpWrapper = new HttpWrapper
    val response = httpWrapper.post(url, "", timeout)
    response.getStatus match {
      case Status.OK => {
        Json.parse(response.getResponse)
      }
      case _ =>
        Logger.error(s"Access control service could not find user [$userId]. Response ${response.getStatus}:${response.getResponse}")
        Counters.incrementAcSubmissionErrorStatus(response.getStatus)
        new JsBoolean(false)
    }
  }

  def getDaysToExpiration(userId: String): JsValue = {
    val timeout = getIntProperty("ac.timeout")
    val url = getStringProperty("accessControlServiceUrl") + s"/expire/$userId"
    val httpWrapper = new HttpWrapper
    val response = httpWrapper.post(url, "", timeout)
    response.getStatus match {
      case Status.OK => Json.parse(response.getResponse)
      case _ =>
        Logger.error(s"Access control service could not find expiry date for user [$userId]. Response from $url was ${response.getStatus}:${response.getResponse}")
        Counters.incrementAcSubmissionErrorStatus(response.getStatus)
        new JsBoolean(false)
    }
  }
}


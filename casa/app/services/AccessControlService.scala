package services

import play.api.Logger
import play.api.libs.json._
import play.api.http.Status
import scala.language.postfixOps
import scala.language.implicitConversions
import monitoring.Counters

object AccessControlServiceImpl extends AccessControlService

class AccessControlService extends CasaRemoteService {
  override def getUrlPropertyName = "accessControlServiceUrl"

  override def getTimeoutPropertyName = "ws.timeout"

  override def getDefaultUrl = "http://localhost:9003"

  def findByUserId(userId: String): JsValue =
    s"$url/user/$userId" post { response =>
      response.status match {
        case Status.OK => response.json
        case _ =>
          Logger.error(s"Access control service could not find user [$userId]. Response ${response.status}:${response.body}")
          Counters.incrementAcSubmissionErrorStatus(response.status)
          new JsBoolean(false)
      }
    } exec()

  def getDaysToExpiration(userId: String): JsValue =
    s"$url/expire/$userId" post { response =>
      response.status match {
      case Status.OK => response.json
      case _ =>
        Logger.error(s"Access control service could not find expiry date for user [$userId]. Response ${response.status}:${response.body}")
        Counters.incrementAcSubmissionErrorStatus(response.status)
        new JsBoolean(false)
      }
    } exec()
}


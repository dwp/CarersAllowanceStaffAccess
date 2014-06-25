package services

import play.api.libs.json._
import play.api.http.Status
import scala.language.postfixOps
import scala.language.implicitConversions
import utils.HttpUtils.HttpMethodWrapper

trait AccessControlService extends CASARemoteService {
  override def getUrlPropertyName = "accessControlServiceUrl"

  override def getTimeoutPropertyName = "ws.timeout"

  override def getDefaultUrl = "http://localhost:9003"

  implicit def stringGetWrapper(url: String) = new HttpMethodWrapper(url, timeout)

   def findByUserId(userId: String): JsValue =
    s"$url/user/$userId" post { response =>
      response.status match {
        case Status.OK => response.json
        case Status.BAD_REQUEST => new JsBoolean(false)
      }
    } exec()

   def getDaysToExpiration(userId: String): JsValue =
    s"$url/expire/$userId" post { response =>
      response.status match {
        case Status.OK => response.json
        case Status.BAD_REQUEST => new JsBoolean(false)
      }
    } exec()

}

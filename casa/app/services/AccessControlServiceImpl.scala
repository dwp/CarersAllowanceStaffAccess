package services

import scala.concurrent.duration._
import play.api.libs.json._
import play.api.{Logger, Play}
import play.api.http.Status
import scala.language.postfixOps
import scala.language.implicitConversions
import scala.Some
import utils.HttpUtils.HttpMethodWrapper

object AccessControlServiceImpl extends AccessControlService {
  val url = getUrl
  val timeout = Play.configuration(Play.current).getInt("ws.timeout").getOrElse(30).seconds

  implicit def stringGetWrapper(url: String) = new HttpMethodWrapper(url, timeout)

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  def getUrl = Play.configuration(Play.current).getString("accessControlServiceUrl") match {
    case Some(s) if s.length > 0 => Logger.info(s"Getting accessControlServiceUrl value ($s)");s
    case _ => Logger.info("Getting default url value"); "http://localhost:9003"
  }

  override def findByUserId(userId: String): JsValue =
    s"$url/user/$userId" post { response =>
      response.status match {
        case Status.OK => response.json
        case Status.BAD_REQUEST => new JsBoolean(false)
      }
    } exec()
}

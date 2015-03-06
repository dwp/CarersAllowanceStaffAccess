package monitoring

import app.ConfigProperties._
import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheck.Result
import play.api.http.Status
import play.api.libs.ws.WSResponse
import utils.HttpUtils.HttpMethodWrapper

import scala.concurrent.duration._
import scala.language.{implicitConversions, postfixOps}


/**
 * Checks connection to renderService.
 */
class RenderServiceConnectionCheck extends HealthCheck {

  implicit def stringWrapper(url:String) = new HttpMethodWrapper(url, getProperty("render.timeout",60).seconds)

  override def check(): Result = {
    val submissionServerEndpoint = getProperty("renderServiceUrl","NotDefined") + "/ping"

    submissionServerEndpoint get { response: WSResponse =>
      response.status match {
        case Status.OK =>
          Result.healthy
        case status@_ =>
          Result.unhealthy(s"Render Service ping failed: ${status}.")
      }
    }

  }

}

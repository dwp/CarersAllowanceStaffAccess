package monitoring

import app.ConfigProperties._
import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheck.Result
import play.api.http.Status
import play.api.libs.ws.Response
import utils.HttpUtils.HttpMethodWrapper

import scala.concurrent.duration._
import scala.language.{implicitConversions, postfixOps}



/**
 * Ping ClaimService to check connection
 */
class ClaimServiceConnectionCheck extends HealthCheck {

  implicit def stringWrapper(url:String) = new HttpMethodWrapper(url, getProperty("cs.timeout",60).seconds)

  override def check(): Result = {
    val submissionServerEndpoint = getProperty("claimsServiceUrl","NotDefined") + "/ping"

      submissionServerEndpoint get { response: Response =>
      response.status match {
        case Status.OK =>
          Result.healthy
        case status@_ =>
          Result.unhealthy(s"Claim Service ping failed: ${status}.")
      }
    }

  }

}

package monitoring

import app.ConfigProperties._
import gov.dwp.carers.CADSHealthCheck
import gov.dwp.carers.CADSHealthCheck.Result
import play.api.http.Status
import utils.HttpUtils.HttpWrapper
import scala.language.{implicitConversions, postfixOps}

/**
 * Ping ClaimService to check connection
 */
class ClaimServiceConnectionCheck extends CADSHealthCheck(s"${getProperty("application.name", default="sa")}", getProperty("application.version", default="x1").takeWhile(_ != '-')) {

  override def check(): Result = {
    val url = getProperty("claimsServiceUrl", "NotDefined") + "/ping"
    val timeout = getProperty("cs.timeout", 60000)
    val httpWrapper = new HttpWrapper
    val response = httpWrapper.get(url, timeout)
    response.getStatus match {
      case Status.OK =>
        Result.healthy
      case status@_ =>
        Result.unhealthy(s"Claim Service ping failed: ${status} from $url with timeout $timeout.")
    }
  }
}

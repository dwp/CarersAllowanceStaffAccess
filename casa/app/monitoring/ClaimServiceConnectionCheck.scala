package monitoring

import app.ConfigProperties._
import gov.dwp.carers.CADSHealthCheck
import gov.dwp.carers.CADSHealthCheck.Result
import play.api.http.Status
import utils.HttpWrapper
import scala.language.{implicitConversions, postfixOps}

/**
 * Ping ClaimService to check connection
 */
class ClaimServiceConnectionCheck extends CADSHealthCheck(s"${getStringProperty("application.name", throwError = false)}", getStringProperty("application.version", throwError = false).takeWhile(_ != '-')) {

  override def check(): Result = {
    val url = getStringProperty("claimsServiceUrl") + "/ping"
    val timeout = getIntProperty("cs.timeout")
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

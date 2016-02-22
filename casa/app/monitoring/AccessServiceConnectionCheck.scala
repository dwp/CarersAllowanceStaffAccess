package monitoring

import app.ConfigProperties._
import gov.dwp.carers.CADSHealthCheck
import gov.dwp.carers.CADSHealthCheck.Result
import play.api.http.Status
import play.api.libs.ws.WSResponse
import utils.HttpUtils.HttpMethodWrapper
import scala.concurrent.duration._
import scala.language.{implicitConversions, postfixOps}

/**
 * Ping ClaimService to check connection
 */
class AccessServiceConnectionCheck extends CADSHealthCheck(s"${getProperty("application.name", default="sa")}", getProperty("application.version", default="x1").takeWhile(_ != '-')) {

  implicit def stringWrapper(url:String) = new HttpMethodWrapper(url, getProperty("ac.timeout",60).seconds)

  override def check(): Result = {
    val submissionServerEndpoint = getProperty("accessControlServiceUrl","NotDefined") + "/ping"

      submissionServerEndpoint get { response: WSResponse =>
      response.status match {
        case Status.OK =>
          Result.healthy
        case status@_ =>
          Result.unhealthy(s"Access Control Service ping failed: ${status}.")
      }
    }

  }

}

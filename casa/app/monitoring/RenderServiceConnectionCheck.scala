package monitoring

import app.ConfigProperties._
import gov.dwp.carers.CADSHealthCheck
import gov.dwp.carers.CADSHealthCheck.Result
import play.api.http.Status
import utils.HttpUtils.HttpWrapper
import scala.language.{implicitConversions, postfixOps}

class RenderServiceConnectionCheck extends CADSHealthCheck(s"${getStringProperty("application.name", throwError = false)}", getStringProperty("application.version", throwError = false).takeWhile(_ != '-')) {

  override def check(): Result = {
    val url = getStringProperty("renderServiceUrl") + "/ping"
    val timeout = getIntProperty("render.timeout")
    val httpWrapper = new HttpWrapper
    val response = httpWrapper.get(url, timeout)
    response.getStatus match {
      case Status.OK =>
        Result.healthy
      case status@_ =>
        Result.unhealthy(s"Render Service ping failed: ${status} for $url with timeout $timeout.")
    }
  }
}

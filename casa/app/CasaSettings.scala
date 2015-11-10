import java.net.InetAddress

import app.ConfigProperties._
import controllers.Auth
import org.slf4j.MDC
import play.api.i18n.Lang
import play.api.{Logger, Application, GlobalSettings}
import play.api.mvc._
import play.api.mvc.Results._
import play.api.http.HeaderNames._

import monitoring.CasaMonitorRegistration
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

import scala.concurrent.Future

/**
 * The MonitorFilter handles all the metrics and health checks. The DwpCSRFilter activates CSRF when not in test mode.
 */
class CasaSettings extends CasaMonitorRegistration with GlobalSettings {

  override def onStart(app: Application): Unit = {
    MDC.put("httpPort", getProperty("http.port", "Value not set"))
    MDC.put("hostName", Option(InetAddress.getLocalHost.getHostName).getOrElse("Value not set"))
    MDC.put("envName", getProperty("env.name", "Value not set"))
    MDC.put("appName", getProperty("app.name", "Value not set"))
    Logger.info("SA is now starting")
    super.onStart(app)

    registerReporters()
    registerHealthChecks()
    Logger.info("SA started")
  }

  override def onStop(app: Application): Unit = {
    Logger.info("SA is now stopping")
  }

  /**
   * Intercept requests to check for session timeout
   * @param request the incoming request
   * @return
   */
  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
    val timeout: Long = play.api.Play.current.configuration.getLong("play.http.session.maxAge").getOrElse(0l)
    val authController = play.api.Play.current.injector.instanceOf[Auth]

    // could also filter out bad request; also find a smarter way to test contains
    if(request.path.contains("assets") || request.path.contains("login")||request.path.contains("logout") ||request.path.contains("password") || request.path.contains("/report/") )
      super.onRouteRequest(request)
    else {
      request.session.get("currentTime") match {
        case Some(time) =>
          // difference in ms
          val deltaMs = (System.nanoTime() - time.toLong)/1000000
          // if less than 30min ok, else session timeout
          if(deltaMs < timeout*60*1000)  {
            super.onRouteRequest(request)
          }
          else Some(authController.login)
        case _ => Some(authController.login)
      }
    }
  }

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] = {
    val errorMsg = "Unexpected error."
    Logger.error (errorMsg + ex.getMessage,ex)
    Future (Ok(views.html.common.error ("/", errorMsg)(Lang.defaultLang, Request (request, AnyContentAsEmpty)))
      .withHeaders(CACHE_CONTROL -> "no-cache, no-store","X-Frame-Options" -> "SAMEORIGIN"))
  }
}


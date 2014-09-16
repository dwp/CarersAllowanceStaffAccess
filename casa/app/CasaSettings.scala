import java.net.InetAddress

import app.ConfigProperties._
import controllers.Auth
import org.slf4j.MDC
import play.api.{Logger, Application, GlobalSettings}
import play.api.mvc._
import utils.csrf.DwpCSRFFilter
import scala.Some
import play.Play
import utils.Injector
import monitor.MonitorFilter
import monitoring._
import monitoring.CasaMonitorRegistration

class CasaSettings extends WithFilters(MonitorFilter, DwpCSRFFilter()) with Injector with CasaMonitorRegistration with GlobalSettings {

  this: Injector =>

  lazy val  authController = resolve(classOf[Auth])

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
    val timeout = Play.application().configuration().getString("application.session.maxAge").toLong

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

  override def getControllerInstance[A](controllerClass: Class[A]): A = resolve(controllerClass)

}


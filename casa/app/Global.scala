import play.api.GlobalSettings
import play.api.mvc._
import scala.Some
import play.Play

object Global extends GlobalSettings {

//  implicit def anyWithIn[A](a: A) = new {
//    def in(as: A*) = as.exists(_ == a)
//  }
  /**
   * Intercept requests to check for session timeout
   * @param request the incoming request
   * @return
   */
  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
    val timeout = Play.application().configuration().getString("application.session.maxAge").toLong

    // could also filter out bad request; also find a smarter way to test contains
    if(request.path.contains("assets") || request.path.contains("login")||request.path.contains("logout") ||request.path.contains("password") )
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
          else Some(controllers.Auth.login)
        case _ => Some(controllers.Auth.login)
      }
    }
  }
}


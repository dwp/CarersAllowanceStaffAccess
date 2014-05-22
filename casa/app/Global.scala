import play.api.GlobalSettings
import play.api.mvc._

object Global extends GlobalSettings {

  /**
   * Intercept requests to check for session timeout
   * @param request
   * @return
   */
  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
    // could also filter out bad request
    if(request.path.contains("assets")|| request.path.contains("login")||request.path.contains("logout") ) super.onRouteRequest(request)
    else {
      request.session.get("currentTime") match {
        case Some(time) =>
          // difference in ms
          val deltaMs = (System.nanoTime() - time.toLong)/1000000
          // if less than 30min ok, else session timeout
          if(deltaMs < 30*60*1000)  {
            super.onRouteRequest(request)
          }
          else Some(controllers.Auth.login)
        case _ => Some(controllers.Auth.login)
      }
    }
  }
}
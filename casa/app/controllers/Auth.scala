package controllers

import play.api.mvc._
import services.{AccessControlService, PasswordService}
import play.api.data._
import play.api.data.Forms._
import views.html
import scala.Predef._
import play.api.Logger
import monitoring.Counters

class Auth extends Controller {

  this: AccessControlService =>

  val loginForm = Form(
    tuple(
      "userId" -> text,
      "password" -> text
    ) verifying ("Invalid user id or password",
      result => result match {case (userId, password) => checkUser(userId, password)}
    )
  )

  def checkUser(userId: String, inputPassword: String): Boolean = {
      val userJson =  findByUserId(userId)
      val password = (userJson \ "password").as[String]

      if(password.length() > 4) {
        if (PasswordService.checkPassword(inputPassword, password.toString)) true
        else false
      }
      else false
  }

  def checkPassword(userId: String): Boolean = {
    val userJson =  getDaysToExpiration(userId)
    if(userJson.toString().equalsIgnoreCase("false")) false
    else {
      val days = userJson.as[Int]
      if(days <= 0) false
      else true
    }
  }

  /**
   * Login page.
   */
  def login = Action { implicit request =>
    Ok(html.login(loginForm))
  }

  /**
   * Handle login form submission.
   */
  def authenticate = Action { implicit request =>
    try {
      loginForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.login(formWithErrors)),
        user => Redirect(routes.Application.index).withSession("userId" -> user._1, "days"->getDaysToExpiration(user._1).toString(), "currentTime"->System.nanoTime().toString)
      )
    } catch {
    case e: Exception =>
      Logger.error(s"Could not connect to the access service",e)
      Ok(views.html.common.error("login", "Could not connect to the access control service."))
    }
  }

  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(controllers.routes.Auth.login).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }
}

/**
 * Provide security features
 */
trait Secured {

  /**
   * Retrieve the connected user id.
   */
  private def username(request: RequestHeader) = request.session.get("userId")

  /**
   * Redirect to login if the user is not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Auth.login)


  /**
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) { user =>
    Action{request =>
      f(user)(request).withSession("userId"->user, "days"-> request.session.get("days").getOrElse(""), "currentTime"->System.nanoTime().toString)
    }
  }

}
package controllers

import play.api.http.HeaderNames._
import play.api.mvc._
import services.{AccessControlService, PasswordService}
import play.api.data._
import play.api.data.Forms._
import views.html
import scala.Predef._
import play.api.Logger
import scala.util.{Failure, Success, Try}
import app.ConfigProperties._
import utils.ApplicationUtils

class Auth extends Controller {

  this: AccessControlService =>

  val loginForm = Form(
    tuple(
      "userId" -> text,
      "password" -> text
    ) verifying("Staff ID should be numerals only and 8 characters long.",
      result => result match {case (userId, password) => validateUserId(userId)}
    )
      verifying ("Invalid user id or password",
      result => result match {case (userId, password) => checkUser(userId, password)}
    )
  )

  def validateUserId(userId:String) = {
    val restrictedStringPattern = """^[0-9]{8}$""".r
    restrictedStringPattern.pattern.matcher(userId).matches
  }

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
  def login =
    Action { implicit request =>
      Ok(html.login(loginForm)).withHeaders(CACHE_CONTROL -> "no-cache, no-store")
        .withHeaders("X-Frame-Options" -> "SAMEORIGIN").withCookies(request.cookies.toSeq.filterNot( _.name == "CASAVersion") :+ Cookie("CASAVersion", "1.2"): _*) // stop click jacking)
    }


  /**
   * Handle login form submission.
   */
  def authenticate = Action { implicit request =>
    try {
      loginForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.login(formWithErrors)),
        user => Redirect(routes.Application.index).withSession("userId" -> user._1, "days"->getDaysToExpiration(user._1).toString(), "currentTime"->System.nanoTime().toString).withHeaders(CACHE_CONTROL -> "no-cache, no-store")
          .withHeaders("X-Frame-Options" -> "SAMEORIGIN") // stop click jacking
      )
    } catch {
    case e: Exception =>
      Logger.error(s"Could not connect to the access service",e)
      Ok(views.html.common.error(ApplicationUtils.startPage, "Could not connect to the access control service.")).withHeaders(CACHE_CONTROL -> "no-cache, no-store")
        .withHeaders("X-Frame-Options" -> "SAMEORIGIN") // stop click jacking
    }
  }

  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(controllers.routes.Auth.login).discardingCookies(DiscardingCookie(getProperty("csrf.cookie.name",""), secure= getProperty("csrf.cookie.secure",false))).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }
}

/**
 * Provide security features
 */
trait Secured {
  import play.api.mvc.Results._

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
    Action{implicit request =>

      Try(
        f(user)(request).withSession("userId"->user, "days"-> request.session.get("days").getOrElse(""), "currentTime"->System.nanoTime().toString)
      ) match {
        case Success(s) => s
        case Failure(e) =>
          val errorMsg = request.path match {
            case s if s.startsWith("/render") => "Could not connect to the render service"
            case _ => "Unexpected error"
          }
          Logger.error(errorMsg,e)
          Ok(views.html.common.error(ApplicationUtils.startPage, errorMsg)).withHeaders(CACHE_CONTROL -> "no-cache, no-store")
            .withHeaders("X-Frame-Options" -> "SAMEORIGIN")
      }
    }
  }

  protected def withSecureHeaders(result:Result): Result = {
    result.withHeaders(CACHE_CONTROL -> "no-cache, no-store")
      .withHeaders("X-Frame-Options" -> "SAMEORIGIN")
  }

}
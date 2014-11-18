package controllers

import play.api.mvc._
import play.api.Logger
import services.PasswordService
import play.api.data.Forms._
import play.api.data._
import views.html
import models.PasswordData
import play.api.data.validation._
import play.api.data.validation.ValidationError
import java.net.URLEncoder

object Password extends Controller {

  val passwordForm = Form(
    mapping (
      "userId" -> text,
      "password1" -> text.verifying(validPassword),
      "password2" -> text.verifying(validPassword)
    )(PasswordData.apply)(PasswordData.unapply)
      .verifying("Passwords do not match", checkPassword _)
    )

  private def checkPassword(form: PasswordData) = form.password1==form.password2

  private def validPassword: Constraint[String] = Constraint[String]("constraint.password") { password =>
    val passwordPattern = """^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?!.*\s).{9,20}$""".r

    passwordPattern.pattern.matcher(password).matches match {
      case true => Valid
      case false => Invalid(ValidationError("Invalid password. Password must be 9 characters long with a mix of lower and uppercase characters and numbers. "))
    }
  }

  /**
   * Display the create password page.
   */
  def display = Action { implicit request =>
    Ok(html.password(passwordForm)).withHeaders(CACHE_CONTROL -> "no-cache, no-store")
      .withHeaders("X-Frame-Options" -> "SAMEORIGIN")
  }

  /**
   * Handle password form submission.
   */
  def digestPassword = Action { implicit request =>
    passwordForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.password(formWithErrors)),
      passwordData => {
        /* binding success, we get the actual value. */
        val pass = digestPasswordForUser(passwordData.userId, passwordData.password1)
        Ok(html.displayDigestedPassword(URLEncoder.encode(pass, "UTF-8"), passwordData.userId)).withHeaders(CACHE_CONTROL -> "no-cache, no-store")
          .withHeaders("X-Frame-Options" -> "SAMEORIGIN")
      }
    )
  }

  /**
   * Encrypt a user's password
   *
   * @param userId - the staff id of the user
   * @param password - plain user password
   * @return JsObject - the digested password as a json obj
   */
  def digestPasswordForUser(userId: String, password: String) = {
    Logger.debug(s"Digesting (encrypting) password for $userId")
    PasswordService.encryptPassword(password)
  }
}

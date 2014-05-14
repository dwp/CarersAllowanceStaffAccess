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


object Password extends Controller {

  val passwordForm = Form(
    mapping (
      "userId" -> text,
      "password1" -> text.verifying(validPassword),
      "password2" -> text.verifying(validPassword)
    )(PasswordData.apply)(PasswordData.unapply)
      .verifying("Passwords do not match", checkPassword _)
    )

  def checkPassword(form: PasswordData) = form.password1==form.password2

  def validPassword: Constraint[String] = Constraint[String]("constraint.password") { password =>
    val passwordPattern = """^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?!.*\s).{9,20}$""".r

    passwordPattern.pattern.matcher(password).matches match {
      case true => Valid
      case false => Invalid(ValidationError("Invalid password. Password must be 9 characters long with a mix of lower and uppercase characters and numbers. "))
    }
  }

  /**
   * Login page.
   */
  def display = Action { implicit request =>
    Ok(html.password(passwordForm))
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
        Ok(html.displayDigestedPassword(pass, passwordData.userId))
      }
    )
  }

  def displayEncryptedPassword = Action { implicit request =>
    Ok(html.password(passwordForm))
  }

  /**
   * Encrypt a user password
   *
   * @param userId - the staff id of the user
   * @param password - plain user password
   * @return JsObject - the digested password as a json obj
   */
  def digestPasswordForUser(userId: String, password: String) = {
    Logger.debug(s"Digesting (encrypting) password for $userId")

    val digestedPassword = PasswordService.encryptPassword(password)

    digestedPassword
  }
}

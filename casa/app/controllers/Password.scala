package controllers

import play.api.mvc._
import play.api.Logger
import services.PasswordService


object Password extends Controller {

  /**
   * Encrypt a user password
   *
   * @param userId - user id
   * @param password - plain user password
   * @return JsObject - the digested password as a json obj
   */
  def digestPassword(userId: String, password: String) = Action {
    Logger.debug(s"Digesting (encrypting) password for $userId")

    val digestedPassword = PasswordService.encryptPassword(password)

    if(!digestedPassword.isEmpty) Ok(digestedPassword)
    else BadRequest
  }
}

package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json._

object Claims extends Controller {
  def retrieve = Action { implicit request =>
    Ok("")
  }
}

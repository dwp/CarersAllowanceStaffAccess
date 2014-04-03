package controllers

import play.api.mvc.{Action, Controller}
import models.view.ClaimingData
import models.view.ClaimingData._
import play.api.libs.json._

object Claims extends Controller with ClaimingData {
  def retrieve = Action { implicit request =>
    Ok("")
  }
}

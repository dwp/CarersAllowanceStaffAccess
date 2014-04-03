package controllers

import play.api.mvc.{Action, Controller}

object Errors extends Controller {

  def timeout = Action {
    Ok(views.html.error("Timeout"))
  }

}

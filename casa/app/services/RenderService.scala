package services

import app.ConfigProperties._
import play.api.Logger
import services.mock.RenderServiceStub

/**
 * Interface of services that implements rendering to htnml of an xml claim.
 */
trait RenderService {
  def claimHtml(xml: String): Option[String]
}

trait RenderServiceComponent {
  val renderService = if (getProperty("stub.renderService", default=true)) {
    Logger.warn("Using stub pdf service.")
    new RenderServiceStub()
  }
  else {
    Logger.warn("Using normal pdf service.")
    new RenderServiceImpl()
  }

  def apply() = renderService
}

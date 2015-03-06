package services

import app.ConfigProperties._
import play.api.Logger
import services.mock.RenderServiceStub

trait RenderService {
  def claimHtml(xml: String): Option[String]
}

trait RenderServiceComponent {
  val renderService = if (getProperty("stub.pdfService", true)) {
    Logger.warn("Using stub pdf service.")
    new RenderServiceStub()
  }
  else {
    new RenderServiceImpl()
  }

  def apply() = renderService
}

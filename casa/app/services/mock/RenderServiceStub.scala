package services.mock

import services.RenderService

class RenderServiceStub extends RenderService {
  override def claimHtml(xml: String): Option[String] = Some(RenderServiceStub.html)
}

object RenderServiceStub {
  def apply() = new RenderServiceStub

  def html = {
    scala.io.Source.fromURL(getClass.getResource("/facadeHtml.txt")).mkString
  }
}

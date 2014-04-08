package services.mock

import services.PdfService


class PDFServiceStub extends PdfService{
  override def claimHtml(transactionId: String): String = PDFServiceStub.html
}

object PDFServiceStub {

  def apply() = new PDFServiceStub
  
  def html = {
    scala.io.Source.fromURL(getClass.getResource("/facadeHtml.txt")).mkString
  }

}

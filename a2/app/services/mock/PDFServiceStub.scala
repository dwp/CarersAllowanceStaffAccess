package services.mock

import services.PdfService


class PDFServiceStub extends PdfService{
  override def claimHtml(transactionId: String): String = views.html.mock.claimHtmlStub(transactionId).body
}

object PDFServiceStub {

  def apply() = new PDFServiceStub

}

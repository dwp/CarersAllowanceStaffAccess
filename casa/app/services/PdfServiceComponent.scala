package services

import services.mock.PDFServiceStub


trait PdfServiceComponent {

  val pdfService = PDFServiceStub()
}

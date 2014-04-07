package services.mocks

import services.ClaimsService
import org.joda.time.{LocalDate, DateTime}
import play.api.libs.json.{JsArray}

class MockErrorClaimsService(cause: => Option[JsArray]) extends ClaimsService {
  override def claims(date: LocalDate) = {
    cause
  }

  override def claimsFiltered(date: LocalDate, status: String) = {
    cause
  }

  override def fullClaim(transactionId: String) = {
    cause
  }

  override def updateClaim(transactionId: String, status: String): Boolean = {
    false
  }
}

object MockErrorClaimsService {
  def apply(cause: => Option[JsArray]) = new MockErrorClaimsService(cause)
}

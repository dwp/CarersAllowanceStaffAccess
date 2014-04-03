package services.mocks

import services.ClaimsService
import org.joda.time.DateTime
import play.api.libs.json.{Json, JsArray}

class MockErrorClaimsService(cause: => Option[JsArray]) extends ClaimsService {
  override def claimsByDate(date: DateTime): Option[JsArray] = {
    cause
  }
}

object MockErrorClaimsService {
  def apply(cause: => Option[JsArray]) = new MockErrorClaimsService(cause)
}

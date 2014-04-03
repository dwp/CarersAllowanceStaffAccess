package services.mock

import services.ClaimsService
import org.joda.time.DateTime
import play.api.libs.json._

class ClaimsServiceStub extends ClaimsService {
  override def claimsByDate(date: DateTime): Option[JsArray] = {
    Some(Json.toJson(ClaimSummary.list).asInstanceOf[JsArray])
  }
}

object ClaimsServiceStub {
 def apply() = new ClaimsServiceStub
}


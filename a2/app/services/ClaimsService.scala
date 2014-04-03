package services

import org.joda.time.DateTime
import play.api.libs.json._

trait ClaimsService {
  def claimsByDate(date: DateTime): Option[JsArray]
}
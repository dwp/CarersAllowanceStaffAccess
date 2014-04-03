package services

import org.joda.time.DateTime
import play.api.libs.json._

trait ClaimsService {
  def claims(date: DateTime): Option[JsArray]
  def claimsFiltered(date: DateTime, status: String): Option[JsArray]
}
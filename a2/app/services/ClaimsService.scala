package services

import org.joda.time.{LocalDate, DateTime}
import play.api.libs.json._

trait ClaimsService {
  def claims(date: LocalDate): Option[JsArray]
  def claimsFiltered(date: LocalDate, status: String): Option[JsArray]
  def fullClaim(transactionId: String): Option[JsValue]
  def updateClaim(transactionId: String, status: String): Boolean
}
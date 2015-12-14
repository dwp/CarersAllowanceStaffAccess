package services

import org.joda.time.LocalDate
import play.api.libs.json.{JsValue, JsBoolean, JsObject, JsArray}

/**
 * Created by rubendiaz on 09/11/15.
 */
trait ClaimService {

  def getClaims(originTag: String, date: LocalDate): Option[JsArray]

  def getCircs(originTag: String, date: LocalDate): Option[JsArray]

  def claimsFilteredBySurname(originTag: String, date: LocalDate, sortBy: String): Option[JsArray]

  def claimsFiltered(originTag: String, date: LocalDate, status: String): Option[JsArray]

  def claimNumbersFiltered(originTag: String, status: String*): JsObject

  def countOfClaimsForTabs(originTag: String, date: LocalDate): JsObject

  def updateClaim(transactionId: String, status: String): JsBoolean

  def fullClaim(transactionId: String, originTag: String): Option[JsValue]

  def buildClaimHtml(transactionId: String, originTag: String): Option[String]

  def getOldClaims(originTag: String): Option[JsArray]

  def purgeOldClaims(originTag: String): JsBoolean
}

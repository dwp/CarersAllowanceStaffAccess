package services
import org.joda.time.LocalDate
import play.api.libs.json.{JsValue, JsBoolean, JsObject, JsArray}

/**
 * Created by rubendiaz on 09/11/15.
 */
trait ClaimService {

  def getClaims(date: LocalDate): Option[JsArray]

  def getCircs(date: LocalDate): Option[JsArray]

  def claimsFilteredBySurname(date: LocalDate, sortBy: String): Option[JsArray]

  def claimsFiltered(date: LocalDate, status: String): Option[JsArray]

  def claimNumbersFiltered(status: String*): JsObject

  def countOfClaimsForTabs(date: LocalDate): JsObject

  def updateClaim(transactionId: String, status: String): JsBoolean

  def fullClaim(transactionId: String): Option[JsValue]

  def buildClaimHtml(transactionId: String): Option[String]

  def getOldClaims: Option[JsArray]

  def purgeOldClaims(): JsBoolean
}

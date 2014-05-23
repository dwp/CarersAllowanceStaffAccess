package services

import play.api.libs.json.JsValue

trait AccessControlService {
  def findByUserId(userId: String): JsValue
  def getDaysToExpiration(userId: String): JsValue
}

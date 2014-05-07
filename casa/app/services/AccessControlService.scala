package services

import play.api.libs.json.JsBoolean


trait AccessControlService {
  def authenticate(userId: String, password: String): JsBoolean
}

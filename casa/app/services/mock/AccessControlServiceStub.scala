package services.mock

import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import services.AccessControlService


/**
 * I exist so that the app can be run up without the need for external services running
 * I can also be used as a base class for mocking
 */
class AccessControlServiceStub extends AccessControlService {
  override def findByUserId(userId: String): JsValue = {
    Logger.warn("Using stub access control service.")
    //john
    Json.parse( """{"password":"jU8D6oropoq4UTnwGklTCqOc1LFObE2LF/Pb6fahvIjjB73x0uwlkGAh/AWzmIgV"}""")
  }

  override def getDaysToExpiration(userId: String): JsValue = {
    Logger.warn("Using stub access control service.")
    userId match {
      case "12345678" => Json.parse( """2""")
      case _ => Json.parse( """0""")
    }
  }
}

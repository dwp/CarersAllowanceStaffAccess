package services

import play.api.libs.json.{Json, JsValue}


/**
 * I exist so that the app can be run up without the need for external services running
 * I can also be used as a base class for mocking
 */
trait AccessControlServiceStub extends AccessControlService {
  override def findByUserId(userId: String): JsValue = {
    Json.parse( """{"password":"jU8D6oropoq4UTnwGklTCqOc1LFObE2LF/Pb6fahvIjjB73x0uwlkGAh/AWzmIgV"}""")
  }

  override def getDaysToExpiration(userId: String): JsValue = {
    Json.parse( """2""")
  }
}

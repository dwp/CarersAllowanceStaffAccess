package services

import play.api.libs.json.{Json, JsValue}


object AccessControlServiceStub extends AccessControlService {
  def findByUserId(userId: String): JsValue = {
    Json.parse( """{"password":"jU8D6oropoq4UTnwGklTCqOc1LFObE2LF/Pb6fahvIjjB73x0uwlkGAh/AWzmIgV"}""")
  }

  def getDaysToExpiration(userId: String): JsValue = {
    if (userId.equals("expired"))
      Json.parse( """0""")
    else
      Json.parse( """2""")
  }
}

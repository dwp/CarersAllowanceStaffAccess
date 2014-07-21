import play.api.libs.json.{Json, JsValue}
import services.AccessControlServiceStub


trait AccessControlServiceMock extends AccessControlServiceStub {
  override def getDaysToExpiration(userId: String): JsValue = {
    if (userId.equals("expired"))
      Json.parse( """0""")
    else
      Json.parse( """2""")
  }
}

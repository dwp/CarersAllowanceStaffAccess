import org.specs2.mutable._
import controllers.Auth
import play.api.libs.json._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.WithApplication

class AuthSpec extends Specification {
  val validUser = Seq("userId" -> "12345678", "password" -> "john")
  val invalidUser = Seq("userId" -> "blah", "password" -> "blah")
  val expiredUser = Seq("userId" -> "test1", "password" -> "john")

  "Auth" should {
    "render the login page" in new WithApplication() {
      val login = route(FakeRequest(GET, "/login")).get

      status(login) must equalTo(OK)
      contentType(login) must beSome.which(_ == "text/html")
      contentAsString(login) must contain("Login")
    }

    "authenticate valid user" in new WithApplication() {
      val authRequest = FakeRequest().withSession().withFormUrlEncodedBody(validUser: _*)

      val authController = app.injector.instanceOf(classOf[Auth])
      val result = authController.authenticate(authRequest)

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) must not(beSome("/login"))
    }

    "not authenticate invalid user" in new WithApplication() {
      val login = route(FakeRequest(GET, "/login")).get

      val authRequest = FakeRequest().withSession().withFormUrlEncodedBody(invalidUser: _*)

      val authController = app.injector.instanceOf(classOf[Auth])
      val result = authController.authenticate(authRequest)

      status(result) mustEqual BAD_REQUEST
    }

    "logout user" in new WithApplication() {
      val authRequest = FakeRequest().withSession().withFormUrlEncodedBody()

      val authController = app.injector.instanceOf(classOf[Auth])
      val result = authController.logout(authRequest)

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) must beSome("/login")
    }

    "json response string from java must convert to scala json OK" in new WithApplication() {
      val javaJsonAsString = "{\"userId\":\"12345678\"}";

      // Converting using JsString adds "\" in front of the " in the Js string thus cannot be recovered
      val badJson = JsString(javaJsonAsString)
      println( "broken userId:"+ badJson \ "userId" )

      val goodJson=Json.parse(javaJsonAsString)
      println("Good conversion of js userId:"+(goodJson \ "userId").get)
      (goodJson \ "userId").get mustEqual("12345678")
    }
  }
}

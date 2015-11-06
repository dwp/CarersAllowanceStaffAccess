import org.specs2.mutable._
import controllers.Auth
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{Injector, WithApplication}

class AuthSpec extends Specification with Injector {
  val validUser = Seq("userId"-> "12345678", "password"-> "john")
  val invalidUser = Seq("userId"-> "blah", "password"-> "blah")
  val expiredUser = Seq("userId"-> "test1", "password"-> "john")

  "Auth" should {
    "render the login page" in new WithApplication() {
      val login = route(FakeRequest(GET, "/login")).get

      status(login) must equalTo(OK)
      contentType(login) must beSome.which(_ == "text/html")
      contentAsString(login) must contain ("Login")
    }

    "authenticate valid user" in new WithApplication() {
      val authRequest = FakeRequest().withSession().withFormUrlEncodedBody(validUser: _*)

      val authController = resolve(classOf[Auth])
      val result = authController.authenticate(authRequest)

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) must not(beSome("/login"))
    }

    "not authenticate invalid user" in new WithApplication() {
      val login = route(FakeRequest(GET, "/login")).get

      val authRequest = FakeRequest().withSession().withFormUrlEncodedBody(invalidUser: _*)

      val authController = resolve(classOf[Auth])
      val result = authController.authenticate(authRequest)

      status(result) mustEqual BAD_REQUEST
    }

    "logout user" in new WithApplication() {
      val authRequest = FakeRequest().withSession().withFormUrlEncodedBody()

      val authController = resolve(classOf[Auth])
      val result = authController.logout(authRequest)

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) must beSome("/login")
    }
  }
}

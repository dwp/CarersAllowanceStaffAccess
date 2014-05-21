import org.specs2.mutable._
import controllers.Auth
import play.api.test._
import play.api.test.Helpers._

class AuthSpec extends Specification {
  val validUser = Seq("userId"-> "test", "password"-> "john")
  val invalidUser = Seq("userId"-> "blah", "password"-> "blah")
  val expiredUser = Seq("userId"-> "test1", "password"-> "john")

  "Auth" should {

    "render the login page" in new WithApplication {
      val login = route(FakeRequest(GET, "/login")).get

      status(login) must equalTo(OK)
      contentType(login) must beSome.which(_ == "text/html")
      contentAsString(login) must contain ("CASA")
    }

    "authenticate valid user" in new WithApplication() {
      val login = route(FakeRequest(GET, "/login")).get

      val authRequest = FakeRequest().withSession().withFormUrlEncodedBody(validUser: _*)

      val result = Auth.authenticate(authRequest)

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) must beSome("/")
    }

    "not authenticate invalid user" in new WithApplication() {
      val login = route(FakeRequest(GET, "/login")).get

      val authRequest = FakeRequest().withSession().withFormUrlEncodedBody(invalidUser: _*)

      val result = Auth.authenticate(authRequest)

      status(result) mustEqual BAD_REQUEST
    }

    "not authenticate a valid user with expired password" in new WithApplication() {
      val login = route(FakeRequest(GET, "/login")).get

      val authRequest = FakeRequest().withSession().withFormUrlEncodedBody(expiredUser: _*)

      val result = Auth.authenticate(authRequest)

      status(result) mustEqual BAD_REQUEST
    }

    "logout user" in new WithApplication() {
      val authRequest = FakeRequest().withSession().withFormUrlEncodedBody()

      val result = Auth.logout(authRequest)

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) must beSome("/login")
    }
  }
}

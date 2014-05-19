import org.specs2.mutable._
import controllers.Password

import play.api.test._
import play.api.test.Helpers._

class PasswordSpec extends Specification {
  val validPasswords = Seq("userId"-> "test123", "password1"-> "Abc123Abc", "password2"->"Abc123Abc")
  val invalidPassword = Seq("userId"-> "test123", "password1"-> "Abc123Abc", "password2"->"abc")
  val invalidPasswords = Seq("userId"-> "test123", "password1"-> "Abc123Abc", "password2"->"abc")
  val nonMatchingPasswords = Seq("userId"-> "test123", "password1"-> "Abc123Abc", "password2"->"Abc345Abc")

  "Auth" should {

    "render the password management page" in new WithApplication {
      val digestPassword = route(FakeRequest(GET, "/password")).get

      status(digestPassword) must equalTo(OK)
      contentType(digestPassword) must beSome.which(_ == "text/html")
      contentAsString(digestPassword) must contain ("CASA")
    }

    "have error on invalid password" in new WithApplication() {
      val digestPassword = route(FakeRequest(GET, "/password")).get

      val passwordRequest = FakeRequest().withSession().withFormUrlEncodedBody(invalidPassword: _*)

      val result = Password.digestPassword(passwordRequest)

      status(result) mustEqual BAD_REQUEST
    }

    "have 2 errors when both passwords are invalid" in new WithApplication() {
      val digestPassword = route(FakeRequest(GET, "/password")).get

      val passwordRequest = FakeRequest().withSession().withFormUrlEncodedBody(invalidPasswords: _*)

      val result = Password.digestPassword(passwordRequest)

      status(result) mustEqual BAD_REQUEST
    }

    "have error on non matching passwords" in new WithApplication() {
      val digestPassword = route(FakeRequest(GET, "/password")).get

      val passwordRequest = FakeRequest().withSession().withFormUrlEncodedBody(nonMatchingPasswords: _*)

      val result = Password.digestPassword(passwordRequest)

      status(result) mustEqual BAD_REQUEST
    }

    "generate encrypted password on valid input"in new WithApplication() {
      val digestPassword = route(FakeRequest(GET, "/password")).get

      val passwordRequest = FakeRequest().withSession().withFormUrlEncodedBody(validPasswords: _*)

      val result = Password.digestPassword(passwordRequest)

      status(result) mustEqual OK
    }

    "display the digested password and user id on successful encryption"in new WithApplication() {
      val digestPassword = route(FakeRequest(GET, "/password")).get

      val passwordRequest = FakeRequest().withSession().withFormUrlEncodedBody(validPasswords: _*)

      val result = Password.digestPassword(passwordRequest)

      status(result) mustEqual OK

      contentType(result) must beSome.which(_ == "text/html")
      contentAsString(result) must contain ("New password")
      contentAsString(result) must contain ("Staff ID")
    }
  }
}

import controllers.Auth
import org.specs2.mutable._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{LightFakeApplication, WithApplication, WithBrowser}

import scalaz.Inject

class ApplicationSpec extends Specification {
  val userInput = Seq("userId" -> "12345678", "password" -> "john")

  "Application" should {

    "send bad requests to the login page" in new WithApplication {
      val bad = route(FakeRequest(GET, "/boum")).get

      status(bad) must equalTo(OK)

      contentAsString(bad) must contain("login")
    }

    "redirect to the login page when user not authenticated" in new WithApplication(app = new LightFakeApplication()) {
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)

      contentAsString(home) must contain("login")
    }

//    "render the index page when user is authenticated" in new WithApplication {
//      val authController = resolve(Auth.getClass)
//      val login = route(FakeRequest(GET, "/login")).get
//
//      contentAsString(login) must contain("CASA")
//
//      val authRequest = FakeRequest().withSession().withFormUrlEncodedBody(userInput: _*)
//
//      val result = authController.authenticate(authRequest)
//
//      redirectLocation(result) must beSome("/")
//    }

    "render change password page when change password link is clicked on the login page" in new WithBrowser {
      browser.goTo("/login")
      val changePasswordPage = browser.click("#changePasswordLoginscreen")
      changePasswordPage.url() must beEqualTo("/password")
    }

    "render login page when login link is clicked from password management screen " in new WithBrowser {
      browser.goTo("/password")
      val loginPage = browser.click("#backToLoginScreen")
      loginPage.url() must beEqualTo("/login")
    }

  }
}

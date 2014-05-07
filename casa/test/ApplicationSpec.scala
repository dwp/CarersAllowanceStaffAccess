import controllers.Auth
import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class ApplicationSpec extends Specification {
  val userInput = Seq("userId"-> "test", "password"-> "john")

  "Application" should {

    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/boum")) must beNone
    }

    "redirect to the login page when user not authenticated" in new WithApplication {
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(SEE_OTHER)

      redirectLocation(home) must beSome("/login")
    }

    "render the index page when user is authenticated" in new WithApplication {
      val login = route(FakeRequest(GET, "/login")).get

      contentAsString(login) must contain ("CASA")

      val authRequest = FakeRequest().withSession().withFormUrlEncodedBody(userInput: _*)

      val result = Auth.authenticate(authRequest)

      redirectLocation(result) must beSome("/")
    }
  }
}

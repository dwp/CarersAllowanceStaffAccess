import org.specs2.mutable._
import utils.WithBrowser

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */

class IntegrationSpec extends Specification {

  "Application" should {

    "work from within a browser" in new WithBrowser() {
      browser.goTo("/login")

      browser.pageSource must contain("CASA")

      browser.fill("#userId") `with` "12345678"
      browser.fill("#password") `with` "john"
      browser.submit("button[type='submit']")

      browser.pageSource must contain("Claims list")
    }

    "display a message to user with expired password" in new WithBrowser() {
      browser.goTo("/login")

      browser.pageSource must contain("CASA")

      browser.fill("#userId") `with` "12345679"
      browser.fill("#password") `with` "john"
      browser.submit("button[type='submit']")

      browser.pageSource must contain("Your password has expired")
    }
  }
}

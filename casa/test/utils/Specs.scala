package utils

import org.openqa.selenium.WebDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.specs2.execute.{AsResult, Result}
import org.specs2.mutable.Around
import org.specs2.specification.Scope
import play.api.Play._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.test._

// NOTE: Do *not* put any initialisation code in the below classes, otherwise delayedInit() gets invoked twice
// which means around() gets invoked twice and everything is not happy.  Only lazy vals and defs are allowed, no vals
// or any other code blocks.

trait PlaySpecification extends org.specs2.mutable.Specification with play.api.test.PlayRunners with play.api.http.HeaderNames with play.api.http.Status with play.api.http.HttpProtocol with play.api.test.DefaultAwaitTimeout with play.api.test.ResultExtractors with play.api.test.Writeables with play.api.test.RouteInvokers with play.api.test.FutureAwaits {
}

/**
 * Used to run specs within the context of a running application.
 *
 * @param app The fake application
 */
abstract class WithApplication(val app: FakeApplication = new LightFakeApplication()) extends Around with org.specs2.matcher.MustThrownExpectations with org.specs2.matcher.ShouldThrownExpectations with I18nSupport {
  lazy val messagesApi: MessagesApi = current.injector.instanceOf[MessagesApi]
  implicit def implicitApp = app
  override def around[T: AsResult](t: => T): Result = {
    Helpers.running(app)(AsResult.effectively(t))
  }
}

/**
 * Used to run specs within the context of a running server.
 *
 * @param app The fake application
 * @param port The port to run the server on
 */
abstract class WithServer(val app: FakeApplication = new LightFakeApplication(),
                          val port: Int = Helpers.testServerPort) extends Around with Scope {
  implicit def implicitApp = app
  implicit def implicitPort: Port = port

  override def around[T: AsResult](t: => T): Result = Helpers.running(TestServer(port, app))(AsResult.effectively(t))
}


abstract class WithJsBrowser[WEBDRIVER <: WebDriver](app: FakeApplication = new LightFakeApplication()) extends WithBrowsers(WebDriverHelper.createDefaultWebDriver(true), app)

abstract class WithBrowser[WEBDRIVER <: WebDriver](app: FakeApplication = new LightFakeApplication()) extends WithBrowsers(WebDriverHelper.createDefaultWebDriver(false), app)

/**
 * Used to run specs within the context of a running server, and using a web browser
 *
 * @param webDriver The driver for the web browser to use
 * @param app The fake application
 * @param port The port to run the server on
 */
abstract class WithBrowsers[WEBDRIVER <: WebDriver](
                                                    val webDriver: WebDriver = WebDriverHelper.createDefaultWebDriver(false),
                                                    val app: FakeApplication = new LightFakeApplication(),
                                                    val port: Int = Helpers.testServerPort) extends Around with Scope with I18nSupport {

  def this(
            webDriver: Class[WEBDRIVER],
            app: FakeApplication,
            port: Int) = this(WebDriverFactory(webDriver), app, port)
  lazy val messagesApi: MessagesApi = current.injector.instanceOf[MessagesApi]
  implicit def implicitApp = app
  implicit def implicitPort: Port = port

  lazy val browser: TestBrowser = TestBrowser(webDriver, Some("http://localhost:" + port))

  override def around[T: AsResult](t: => T): Result = {
    try {
      Helpers.running(TestServer(port, app))(AsResult.effectively(t))
    } finally {
      browser.quit()
    }
  }
}


object WebDriverHelper {
  def createDefaultWebDriver(enableJs: Boolean = false): WebDriver = {
    new HtmlUnitDriver(enableJs)
  }
}

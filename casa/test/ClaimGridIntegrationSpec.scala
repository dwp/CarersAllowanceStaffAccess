import org.fluentlenium.core.domain.{FluentList, FluentWebElement}
import org.joda.time.format.DateTimeFormat
import org.joda.time.LocalDate
import org.specs2.mutable.{Specification, Tags}
import play.api.test.{FakeApplication, TestBrowser, WithBrowser}
import scala._
import scala.collection.JavaConverters._

class ClaimGridIntegrationSpec extends Specification with Tags {

  val userId = "test"
  val password = "john"

  "Claim Grid" should {

    "Show claims filtered by today's date" in new WithBrowserStub {
      login(browser)

      browser.title() mustEqual "Claims list"
      val today = new LocalDate

      checkCasaDates(today, browser)
    }

    "Only contain received or viewed statuses" in new WithBrowserStub {
      login(browser)

      browser.goTo("/")

      val statuses = browser.$("#claimsTable .status").asScala.toSeq

      checkForStatus(statuses, Seq("received", "viewed"))
    }

    "Filtering by completed only shows completed claims" in new WithBrowserStub {
      login(browser)

      val today = DateTimeFormat.forPattern("ddMMyyyy").print(new LocalDate)
      browser.goTo(s"/filter/$today/completed")

      val statuses = browser.$("#claimsTable .status").asScala.toSeq

      checkForStatus(statuses, Seq("completed"))
    }

    "Filtering by work queue only shows received or viewed claims for default sort a to m" in new WithBrowserStub {
      login(browser)

      val statuses = browser.$("#claimsTable .status").asScala.toSeq

      checkForStatus(statuses, Seq("received", "viewed"))
    }

    "Filtering by surname a to m shows only received or viewed claims for claimants with surnames starting with a to m" in new WithBrowserStub {
      login(browser)

      val today = DateTimeFormat.forPattern("ddMMyyyy").print(new LocalDate)
      browser.goTo(s"/filter/surname/$today/atom")

      val statuses = browser.$("#claimsTable .status").asScala.toSeq

      checkForStatus(statuses, Seq("received", "viewed"))
    }

    "Filtering by surname n to z shows only received or viewed claims for claimants with surnames starting with n to z" in new WithBrowserStub {
      login(browser)

      val today = DateTimeFormat.forPattern("ddMMyyyy").print(new LocalDate)
      browser.goTo(s"/filter/surname/$today/ntoz")

      val statuses = browser.$("#claimsTable .status").asScala.toSeq

      checkForStatus(statuses, Seq("received", "viewed"))
    }

    "Show claims filtered by specified date" in new WithBrowserStub {
      login(browser)

      val yesterday = new LocalDate().minusDays(1)
      val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(yesterday)

      browser.goTo("/filter/" + dateString)

      checkCasaDates(yesterday, browser = browser)
    }

    "Update the claim status for all selected claims when 'Complete Claims' button clicked" in new WithBrowserStub {
      login(browser)

      val transactionId = "20140102071"
      browser.$(s"#$transactionId").click

      browser.$("#completeButton1").click

      browser.pageSource() must not contain transactionId
    }

    "Open html rendered pdf in new tab and checked updated status from received to viewed" in new WithBrowserStub {
      import scala.collection.JavaConverters._

      login(browser)

      val transactionId = "1111070"
      browser.$(s"#row_$transactionId .transactionId a").click

      browser.$(s"#row_$transactionId .status").getText mustEqual "viewed"

      val gridPageHandle = browser.webDriver.getWindowHandle
      val pdfTab = browser.webDriver.getWindowHandles.asScala.toSeq.filterNot(_==gridPageHandle)(0)

      browser.webDriver.switchTo().window(pdfTab)

      browser.title mustEqual s"Claim $transactionId"
    }

    "Open completed claim pdf and check the status hasn't changed" in new WithBrowserStub {
      import scala.collection.JavaConverters._
      val today = DateTimeFormat.forPattern("ddMMyyyy").print(new LocalDate)

      login(browser)

      browser.goTo(s"/filter/$today/completed")

      val transactionId = "1111072"
      browser.$(s"#row_$transactionId .transactionId a").click

      browser.$(s"#row_$transactionId .status").getText mustEqual "completed"

      val gridPageHandle = browser.webDriver.getWindowHandle
      val pdfTab = browser.webDriver.getWindowHandles.asScala.toSeq.filterNot(_==gridPageHandle)(0)

      browser.webDriver.switchTo().window(pdfTab)

      browser.title mustEqual s"Claim $transactionId"
    }

    "Should show circs under the circs tab" in new WithBrowserStub {
      val today = DateTimeFormat.forPattern("ddMMyyyy").print(new LocalDate)

      login(browser)

      browser.goTo(s"/circs/$today")

      val claimTypes = browser.$("#claimsTable .view")
      assertCircsType (claimTypes)
    }

    "Should show claims first then Circs for completed" in new WithBrowserStub {
      val today = DateTimeFormat.forPattern("ddMMyyyy").print(new LocalDate)

      login(browser)

      browser.goTo(s"/filter/$today/completed")

      val claimTypes = browser.$("#claimsTable .view")
      assertClaimTypesOrdering (claimTypes)
    }

    "Sort by transaction id should return the same number of entries as the original list" in new WithBrowserStub {
      login(browser)

      browser.goTo("/")

      val transactionIds = browser.$("#transactionId").asScala.toSeq

      browser.$("#thTransactionId").click()

      val transactionIdsSorted = browser.$("#claimsTable .transactionId").asScala.toSeq

      transactionIds.size must beEqualTo(transactionIdsSorted.size)
    }

    "Sort by name should return the same number of entries as the original list" in new WithBrowserStub {
      login(browser)

      browser.goTo("/")

      val names = browser.$("#claimsTable .name").asScala.toSeq

      browser.$("#thNameId").click()

      val namesSorted = browser.$("#claimsTable .name").asScala.toSeq

      names.size must beEqualTo(namesSorted.size)
    }

    "Should sort by transaction id " in new WithBrowserStub {
      login(browser)

      browser.goTo("/")

      val transactionIds = browser.$("#transactionId").asScala.toList

      browser.$("#thTransactionId").click()

      val transactionIdsSorted = browser.$("#claimsTable .transactionId").asScala.toList

      compareSort(transactionIdsSorted)
    }.pendingUntilFixed("The javascript code does not seem to get executed on click from the test")

    "Should sort by name" in new WithBrowserStub {
      login(browser)

      browser.goTo("/")

      browser.$("#thNameId").click()

      val namesSorted = browser.$("#name").asScala.toList

      compareSort(namesSorted)
    }.pendingUntilFixed("The javascript code does not seem to get executed on click from the test")
  }

  def login(browser: TestBrowser) = {
    browser.goTo("/login")
    browser.fill("#userId") `with` userId
    browser.fill("#password") `with` password
    browser.submit("button[type='submit']")
  }

  def assertCircsType (claimTypes:FluentList[FluentWebElement]) = {

    val circsTypeList = claimTypes.asScala.toSeq.filter(f => f.getText == "circs")

    circsTypeList.size must be_>(0)

    val claimsTypeList = claimTypes.asScala.toSeq.filter(f => f.getText == "claim")

    claimsTypeList must beEmpty

  }

  def assertClaimTypesOrdering (claimTypes:FluentList[FluentWebElement]) = {

    val claimTypeList = claimTypes.asScala.toSeq.filter(f => f.getText == "claim" || f.getText == "circs")

    def isClaimOrCircs(claimType:String) = if(claimType == "claim") 1 else 2

    var previousValue = isClaimOrCircs(claimTypeList.head.getText)

    previousValue must beEqualTo(1)

    claimTypeList.foreach(f => {
      val value = isClaimOrCircs(f.getText)
      value must beGreaterThanOrEqualTo(previousValue)
      previousValue = value
    })
  }

  def checkCasaDates(date: LocalDate, browser: TestBrowser) = {
    val receivedDates = browser.$("#claimsTable .casaDate")

    val dateValue = DateTimeFormat.forPattern("dd/MM/yyyy").print(date)

    for (receivedDate <- receivedDates.asScala.toSeq) {
      receivedDate.getText mustEqual dateValue
    }
  }
  
  def checkForStatus(actualStatuses: Seq[FluentWebElement], expectedStatuses: Seq[String]) {
    actualStatuses.size must be_>(0)

    for (status <- actualStatuses) {
      status.getText must beOneOf(expectedStatuses:_*)
    }
  }

  def compareSort(data: List[FluentWebElement]) {
    data.size must be_>(0)
    var previous = ""
    for (current <- data) {
      current.getText.compareTo(previous) must be_>=(0)
      previous = current.getText
    }

  }
}

class WithBrowserStub extends WithBrowser(app=FakeApplication(additionalConfiguration = Map("enableStub"->"true")))

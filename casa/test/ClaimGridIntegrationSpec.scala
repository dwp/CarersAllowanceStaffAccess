import org.fluentlenium.core.domain.{FluentList, FluentWebElement}
import org.joda.time.format.DateTimeFormat
import org.joda.time.LocalDate
import org.specs2.mutable.Specification
import play.api.test.TestBrowser
import utils.WithBrowser
import scala.collection.JavaConverters._

class ClaimGridIntegrationSpec extends Specification {

  val userId = "12345678"
  val password = "john"
  val regExForCount= "\\((.*?)\\)".r

  "Claim Grid" should {

    "show claims filtered by today's date" in new WithBrowser {
      login(browser)

      browser.title() mustEqual "Claims list"
      val today = new LocalDate

      checkCasaDates(today, browser)
    }

    "only contain received or viewed statuses by default on home page" in new WithBrowser {
      login(browser)

      browser.goTo("/")

      val statuses = browser.$("#claimsTable .status").asScala.toSeq

      checkForStatus(statuses, Seq("received", "viewed"))
    }

    "show only completed claims when filtering by completed" in new WithBrowser {
      login(browser)

      val today = DateTimeFormat.forPattern("ddMMyyyy").print(new LocalDate)
      browser.goTo(s"/filter/$today/completed")

      val statuses = browser.$("#claimsTable .status").asScala.toSeq

      checkForStatus(statuses, Seq("completed"))
    }

    "show received or viewed claims for default sort a to m" in new WithBrowser {
      login(browser)

      val statuses = browser.$("#claimsTable .status").asScala.toSeq

      checkForStatus(statuses, Seq("received", "viewed"))
    }

    "show only received or viewed claims for claimants with surnames a to m when filtering by surname a to m" in new WithBrowser {
      login(browser)

      val today = DateTimeFormat.forPattern("ddMMyyyy").print(new LocalDate)
      browser.goTo(s"/filter/surname/$today/atom")

      val statuses = browser.$("#claimsTable .status").asScala.toSeq

      checkForStatus(statuses, Seq("received", "viewed"))
    }

    "show count on tabs when filtering by surname a to m" in new WithBrowser {
      login(browser)

      val today = DateTimeFormat.forPattern("ddMMyyyy").print(new LocalDate)
      browser.goTo(s"/filter/surname/$today/atom")

      assertCount(browser)
    }

    "show count on tabs when filtering by surname n to z" in new WithBrowser {
      login(browser)

      val today = DateTimeFormat.forPattern("ddMMyyyy").print(new LocalDate)
      browser.goTo(s"/filter/surname/$today/ntoz")

      assertCount(browser)
    }

    "show count when circs tab is pressed" in new WithBrowser {
      val today = DateTimeFormat.forPattern("ddMMyyyy").print(new LocalDate)

      login(browser)

      browser.goTo(s"/circs/$today")

      assertCount(browser)
    }

    "show only received or viewed claims for claimants with surnames n to z when filtering by surname n to z" in new WithBrowser {
      login(browser)

      val today = DateTimeFormat.forPattern("ddMMyyyy").print(new LocalDate)
      browser.goTo(s"/filter/surname/$today/ntoz")

      val statuses = browser.$("#claimsTable .status").asScala.toSeq

      checkForStatus(statuses, Seq("received", "viewed"))
    }

    "show claims filtered by specified date" in new WithBrowser {
      login(browser)

      val yesterday = new LocalDate().minusDays(1)
      val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(yesterday)

      browser.goTo("/filter/" + dateString)

      checkCasaDates(yesterday, browser = browser)
    }

    "update the claim status for all selected claims when 'Complete Claims' button clicked" in new WithBrowser {
      login(browser)

      val transactionId = "20140102071"
      browser.$(s"#transactionId_1").click

      browser.$("#completeButton1").click

      browser.pageSource() must not contain transactionId
    }

    "open html rendered pdf in new tab and checked updated status from received to viewed" in new WithBrowser {
      pending("with browser is not capable of handling the level of JS used")

      login(browser)

      val transactionId = "1111070"
      browser.$(s"#row_$transactionId .transactionId a").click

      browser.$(s"#row_$transactionId .status").getText mustEqual "viewed"

      val gridPageHandle = browser.webDriver.getWindowHandle
      val pdfTab = browser.webDriver.getWindowHandles.asScala.toSeq.filterNot(_==gridPageHandle)(0)

      browser.webDriver.switchTo().window(pdfTab)

      browser.title mustEqual s"Claim $transactionId"
    }

    "open completed claim pdf and check the status hasn't changed" in new WithBrowser {
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

    "show circs under the circs tab" in new WithBrowser {
      val today = DateTimeFormat.forPattern("ddMMyyyy").print(new LocalDate)

      login(browser)

      browser.goTo(s"/circs/$today")

      val claimTypes = browser.$("#claimsTable .claimtype")
      assertCircsType (claimTypes)
    }

    "show claims first then circs on completed tab" in new WithBrowser {
      val today = DateTimeFormat.forPattern("ddMMyyyy").print(new LocalDate)

      login(browser)

      browser.goTo(s"/filter/$today/completed")

      val claimTypes = browser.$("#claimsTable .claimtype")
      assertClaimTypesOrdering (claimTypes)
    }

    "sort by transaction id should return the same number of entries as the original list" in new WithBrowser {
      login(browser)

      browser.goTo("/")

      browser.$("#thTransactionId").click()

      val transactionIdsSorted = browser.$("#claimsTable .transactionId").asScala.toSeq
      val pastTransactionId = browser.$(s"#transactionId_${transactionIdsSorted.size}")
      val lastTransactionId = browser.$(s"#transactionId_${transactionIdsSorted.size - 1}")
      lastTransactionId.size() must beEqualTo(1)
      pastTransactionId.size() must beEqualTo(0)
    }

    "return after a sort the same number of entries as the original list" in new WithBrowser {
      login(browser)

      browser.goTo("/")

      val names = browser.$("#claimsTable .name").asScala.toSeq

      browser.$("#thNameId").click()

      val namesSorted = browser.$("#claimsTable .name").asScala.toSeq

      names.size must beEqualTo(namesSorted.size)
    }

    "sort by transaction id " in new WithBrowser {
      pending("with browser is not capable of handling the level of JS used")
      login(browser)

      browser.goTo("/")

      browser.$("#thTransactionId").click()

      Thread.sleep(1000)

      var transactionIdsSorted = browser.$("#claimsTable .transactionId").asScala.toList

      compareSort(transactionIdsSorted)
    }

    "sort by name" in new WithBrowser {
      login(browser)

      browser.goTo("/")

      browser.$("#thNameId").click()

      Thread.sleep(1000)

      var nameIds = browser.$("#claimsTable .name").asScala.toList

      compareSort(nameIds)
    }

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

  def assertCount(browser:TestBrowser) = {
    val tabCounts = browser.$("ol.clearfix > li").asScala.toSeq.drop(4)

    tabCounts.map(t => {
      regExForCount.findFirstIn(t.getText) match {
        case Some(t) => t.replace("(", "").replace(")", "").toInt must be_>(0)
        case _ => failure
      }
    })
  }

}

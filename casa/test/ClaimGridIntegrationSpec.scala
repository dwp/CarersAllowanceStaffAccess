import org.fluentlenium.core.domain.FluentWebElement
import org.joda.time.format.DateTimeFormat
import org.joda.time.LocalDate
import org.specs2.mutable.{Specification, Tags}
import play.api.test.{TestBrowser, WithBrowser}
import scala.collection.JavaConverters._


class ClaimGridIntegrationSpec extends Specification with Tags {
  "Claim Grid" should {
    "Show claims filtered by today's date" in new WithBrowser {
      browser.goTo("/")
      browser.title() mustEqual("Claims list")
      val today = new LocalDate

      checkCasaDates(today, browser)
    }

    "Only contain received or viewed statuses" in new WithBrowser {
      browser.goTo("/")

      val statuses = browser.$("#claimsTable .status").asScala.toSeq

      checkForStatus(statuses, Seq("received", "viewed"))
    }

    "Filtering by completed only shows completed claims" in new WithBrowser {
      val today = DateTimeFormat.forPattern("ddMMyyyy").print(new LocalDate)
      browser.goTo(s"/filter/$today/completed")

      val statuses = browser.$("#claimsTable .status").asScala.toSeq

      checkForStatus(statuses, Seq("completed"))
    }

    "Filtering by work queue only shows received or viewed claims" in new WithBrowser {
      val today = DateTimeFormat.forPattern("ddMMyyyy").print(new LocalDate)
      browser.goTo(s"/filter/$today")

      val statuses = browser.$("#claimsTable .status").asScala.toSeq

      checkForStatus(statuses, Seq("received", "viewed"))
    }

    "Show claims filtered by specified date" in new WithBrowser {
      val yesterday = new LocalDate().minusDays(1)
      val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(yesterday)

      browser.goTo("/filter/" + dateString)

      checkCasaDates(yesterday, browser = browser)
    }

    "Update the claim status for all selected claims when 'Complete Claims' button clicked" in new WithBrowser {

      browser.goTo("/")

      val transactionId = "20140102071"
      browser.$(s"#$transactionId").click

      browser.$("#completeButton").click

      browser.pageSource() must not contain transactionId
    }

    "Open html rendered pdf in new tab and checked updated status from received to viewed" in new WithBrowser {
      import scala.collection.JavaConverters._
      browser.goTo("/")

      val transactionId = "1111070"
      browser.$(s"#row_$transactionId .transactionId a").click

      browser.$(s"#row_$transactionId .status").getText mustEqual "viewed"

      val gridPageHandle = browser.webDriver.getWindowHandle
      val pdfTab = browser.webDriver.getWindowHandles.asScala.toSeq.filterNot(_==gridPageHandle)(0)

      browser.webDriver.switchTo().window(pdfTab)

      browser.title mustEqual s"Claim $transactionId"
    }

    "Open completed claim pdf and check the status hasn't changed" in new WithBrowser {
      import scala.collection.JavaConverters._
      val today = DateTimeFormat.forPattern("ddMMyyyy").print(new LocalDate)
      browser.goTo(s"/filter/$today/completed")

      val transactionId = "1111072"
      browser.$(s"#row_$transactionId .transactionId a").click

      browser.$(s"#row_$transactionId .status").getText mustEqual "completed"

      val gridPageHandle = browser.webDriver.getWindowHandle
      val pdfTab = browser.webDriver.getWindowHandles.asScala.toSeq.filterNot(_==gridPageHandle)(0)

      browser.webDriver.switchTo().window(pdfTab)

      browser.title mustEqual s"Claim $transactionId"
    }

    "Should show claims first then Circs" in new WithBrowser {
      import scala.collection.JavaConverters._

      browser.goTo("/")
      val claimTypes = browser.$("#claimsTable .view")
      val claimTypeList = claimTypes.asScala.toSeq.filter(f => (((f.getText == "claim") || (f.getText == "circs"))))


      def isClaimOrCircs(claimType:String) = if(claimType == "claim") 1 else 2

      var previousValue = isClaimOrCircs(claimTypeList.head.getText)

      previousValue must beEqualTo(1)

      claimTypeList.foreach(f => {
        val value = isClaimOrCircs(f.getText)
        value must beGreaterThanOrEqualTo(previousValue)
        previousValue = value
      })
    }
  }

  def checkCasaDates(date: LocalDate, browser: TestBrowser) = {
    val receivedDates = browser.$("#claimsTable .casaDate")

    val dateValue = DateTimeFormat.forPattern("dd/MM/yyyy").print(date)

    for (receivedDate <- receivedDates.asScala.toSeq) {
      receivedDate.getText() mustEqual(dateValue)
    }
  }
  
  def checkForStatus(actualStatuses: Seq[FluentWebElement], expectedStatuses: Seq[String]) {
    actualStatuses.size must be_>(0)

    for (status <- actualStatuses) {
      status.getText() must beOneOf(expectedStatuses:_*)
    }
  }
}

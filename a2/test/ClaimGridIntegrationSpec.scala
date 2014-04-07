import org.joda.time.format.DateTimeFormat
import org.joda.time.LocalDate
import org.specs2.mutable.{Specification, Tags}
import play.api.test.WithBrowser
import scala.collection.JavaConverters._

class ClaimGridIntegrationSpec extends Specification with Tags {
  "Claim Grid" should {
    "Show claims filtered by today's date" in new WithBrowser {
      browser.goTo("/")
      browser.title() mustEqual("Claims list")
      val today = DateTimeFormat.forPattern("dd/MM/yyyy").print(new LocalDate)
      val receivedDates = browser.$("#claimsTable .casaDate")

      for (receivedDate <- receivedDates.asScala.toSeq) {
        receivedDate.getText() mustEqual(today)
      }
    }

    "Only contain received or viewed statuses" in new WithBrowser {
      browser.goTo("/")

      val statuses = browser.$("#claimsTable .status").asScala.toSeq

      statuses.size must be_>(0)

      for (status <- statuses) {
        status.getText() must beOneOf("received", "viewed")
      }
    }

    "Filtering by completed only shows completed claims" in new WithBrowser {
      val today = DateTimeFormat.forPattern("ddMMyyyy").print(new LocalDate)
      browser.goTo(s"/filter/$today/completed")

      val statuses = browser.$("#claimsTable .status").asScala.toSeq

      statuses.size must be_>(0)

      for (status <- statuses) {
        status.getText() mustEqual("completed")
      }
    }

    "Filtering by work queue only shows received or viewed claims" in new WithBrowser {
      val today = DateTimeFormat.forPattern("ddMMyyyy").print(new LocalDate)
      browser.goTo(s"/filter/$today")

      val statuses = browser.$("#claimsTable .status").asScala.toSeq

      statuses.size must be_>(0)

      for (status <- statuses) {
        status.getText() must beOneOf("received", "viewed")
      }
    }

    "Show claims filtered by specified date" in new WithBrowser {
      pending
    }
  }
}

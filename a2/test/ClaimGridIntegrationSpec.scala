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
  }
}

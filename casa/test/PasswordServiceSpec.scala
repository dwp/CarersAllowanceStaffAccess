import org.specs2.mutable.{Tags, Specification}
import services.PasswordService

class PasswordServiceSpec extends Specification with Tags {

  "PasswordService" should {

    "encrypt a password given a plain password" in {
      PasswordService.encryptPassword("test") must not beEmpty
    }

    "check that a password is valid" in {
      val check = PasswordService.checkPassword("john", "jU8D6oropoq4UTnwGklTCqOc1LFObE2LF/Pb6fahvIjjB73x0uwlkGAh/AWzmIgV")
      check mustEqual true
    }
  }


}

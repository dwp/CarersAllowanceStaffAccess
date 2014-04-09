package utils

import scala.annotation.tailrec
import org.joda.time.LocalDate


object DateUtils {
  @tailrec
  def daysRec(n:Int,today:LocalDate, localDates: Seq[LocalDate]):Seq[LocalDate] = {
    if (n == 0) localDates
    else {
      val day = today.minusDays(1)
      daysRec( n-1, day, day +: localDates)
    }
  }
}

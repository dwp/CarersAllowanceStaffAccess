package monitoring

import com.codahale.metrics.SharedMetricRegistries
import play.api.Play._

object Counters {
  def incrementAcSubmissionErrorStatus(status:Int) {
    SharedMetricRegistries.getOrCreate(current.configuration.getString("metrics.name").getOrElse("default")).counter(s"ac-submission-error-status-$status").inc()
  }
  def incrementCsSubmissionErrorStatus(status:Int) {
    SharedMetricRegistries.getOrCreate(current.configuration.getString("metrics.name").getOrElse("default")).counter(s"cs-submission-error-status-$status").inc()
  }

  def incrementP1SubmissionErrorStatus(status: Int) {
    SharedMetricRegistries.getOrCreate(current.configuration.getString("metrics.name").getOrElse("default")).counter(s"p1-submission-error-status-$status").inc()
  }
}

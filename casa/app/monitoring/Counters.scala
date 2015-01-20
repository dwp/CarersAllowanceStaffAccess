package monitoring

import com.kenshoo.play.metrics.MetricsRegistry


object Counters {
  def incrementAcSubmissionErrorStatus(status:Int) {
    MetricsRegistry.defaultRegistry.counter(s"ac-submission-error-status-$status").inc()
  }
  def incrementCsSubmissionErrorStatus(status:Int) {
    MetricsRegistry.defaultRegistry.counter(s"cs-submission-error-status-$status").inc()
  }
}

package utils

import javax.inject.Inject

import com.kenshoo.play.metrics.MetricsFilter
import play.api.http.HttpFilters
import utils.csrf.DwpCSRFFilter

class Filters @Inject() (metricsFilter: MetricsFilter) extends HttpFilters {
  val dwpCSRFFilter = new DwpCSRFFilter()
  val filters = Seq(metricsFilter, dwpCSRFFilter)
}

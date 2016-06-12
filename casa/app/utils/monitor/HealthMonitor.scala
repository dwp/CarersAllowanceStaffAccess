package utils.monitor

import gov.dwp.carers.{CADSHealthCheck, CADSHealthCheckRegistry}
import play.api.Logger

import scala.collection.JavaConversions._
import scala.collection.immutable.SortedMap

abstract class HealthMonitor {
  val registry = new CADSHealthCheckRegistry()

  def register(label: String, healthCheck: CADSHealthCheck) {
    registry.register(label, healthCheck)
  }

  def unregister(label: String) {
    registry.unregister(label)
  }

  def runHealthChecks(): SortedMap[String, CADSHealthCheck.Result] = {
    SortedMap(registry.runHealthChecks().toSeq: _*)
  }

  def reportHealth() {
    runHealthChecks().map{ cadsHealthCheck => Logger.info(s"application=${cadsHealthCheck._2.getApplication()}, version=${cadsHealthCheck._2.getVersion()}, ${cadsHealthCheck.toString}") }
  }
}

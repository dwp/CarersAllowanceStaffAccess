package monitoring

import app.ConfigProperties._
import play.api.Logger
import monitor.{HealthMonitor, MonitorRegistration}

trait CasaMonitorRegistration extends MonitorRegistration {

  override def getFrequency: Int = getIntProperty("metrics.frequency", throwError = false)

  override def isLogMetrics: Boolean = getBooleanProperty("metrics.slf4j", throwError = false)

  override def isLogHealth: Boolean = getBooleanProperty("health.logging", throwError = false)

  override   def getHealthMonitor : HealthMonitor = ProdHealthMonitor

  override def registerHealthChecks(): Unit = {
    Logger.info("Health Checks registered.")
    ProdHealthMonitor.register("casa-connection-ac", new AccessServiceConnectionCheck)
    ProdHealthMonitor.register("casa-connection-cs", new ClaimServiceConnectionCheck)
    ProdHealthMonitor.register("casa-connection-renderer", new RenderServiceConnectionCheck())
  }

}
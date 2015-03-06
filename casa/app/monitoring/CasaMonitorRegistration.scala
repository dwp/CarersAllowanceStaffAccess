package monitoring

import app.ConfigProperties._
import play.api.Logger
import utils.Injector
import monitor.{HealthMonitor, MonitorRegistration}

trait CasaMonitorRegistration extends MonitorRegistration {
  this: Injector =>

  override def getFrequency: Int = getProperty("metrics.frequency", default = 1)

  override def isLogMetrics: Boolean = getProperty("metrics.slf4j", default = false)

  override def isLogHealth: Boolean = getProperty("health.logging", default = false)

  override   def getHealthMonitor : HealthMonitor = ProdHealthMonitor

  override def registerHealthChecks(): Unit = {
    Logger.info("Health Checks registered.")
    ProdHealthMonitor.register("casa-connection-ac", new AccessServiceConnectionCheck)
    ProdHealthMonitor.register("casa-connection-cs", new ClaimServiceConnectionCheck)
    ProdHealthMonitor.register("casa-connection-renderer", new RenderServiceConnectionCheck())
  }

}
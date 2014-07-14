package utils

import play.api.Logger

import scala.reflect._
import scala.language.existentials
import services.{AccessControlService, AccessControlServiceStub, ClaimService, ClaimServiceStub}

trait Injector {
  def resolve[A](clazz: Class[A]) = instances(clazz).asInstanceOf[A]

  private lazy val instances: Map[Class[_ <: Any], Any] = {

    def bind[A: ClassTag](instance: A) = classTag[A].runtimeClass -> instance

    val stubEnabled = play.api.Play.current.configuration.getBoolean("enableStub") == Some(true)

    if (stubEnabled) {
      Logger.warn("Using stubs.")
      Map(bind[controllers.Application](new controllers.Application with ClaimServiceStub),
        bind[controllers.Auth](new controllers.Auth with AccessControlServiceStub))
    } else {
      Map(bind[controllers.Application](new controllers.Application with ClaimService),
        bind[controllers.Auth](new controllers.Auth with AccessControlService))
    }
  }
}

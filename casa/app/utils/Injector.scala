package utils

import play.api.Logger
import services.mock.{ClaimServiceStub, AccessControlServiceStub}

import scala.reflect._
import scala.language.existentials
import services.{AccessControlService, ClaimService}

trait Injector {
  def resolve[A](clazz: Class[A]) = instances(clazz).asInstanceOf[A]

  private lazy val instances: Map[Class[_ <: Any], Any] = {

    def bind[A: ClassTag](instance: A) = classTag[A].runtimeClass -> instance

    val stubEnabled = play.api.Play.current.configuration.getBoolean("enableStub") == Some(true)

    if (stubEnabled) {
      Logger.warn("Using stubs.")
      Map(bind[controllers.Application](new controllers.Application(new ClaimServiceStub())),
        bind[controllers.Auth](new controllers.Auth(new AccessControlServiceStub())))
    } else {
      Map(bind[controllers.Application](new controllers.Application(new ClaimService())),
        bind[controllers.Auth](new controllers.Auth(new AccessControlService())))
    }
  }
}

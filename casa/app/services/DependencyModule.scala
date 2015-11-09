package services

import com.google.inject.AbstractModule
import play.api.{Logger, Environment, Configuration}
import services.mock.{ClaimServiceStub, AccessControlServiceStub}


class DependencyModule(environment: Environment, configuration: Configuration) extends AbstractModule {
  override def configure() = {
    configuration.getBoolean("enableStub") match{
      case Some(true) =>
        Logger.info("Using stubs")
        bind(classOf[AccessControlService]).to(classOf[AccessControlServiceStub])
        bind(classOf[ClaimService]).to(classOf[ClaimServiceStub])
      case _ =>
        bind(classOf[AccessControlService]).to(classOf[AccessControlServiceImpl])
        bind(classOf[ClaimService]).to(classOf[ClaimServiceImpl])
    }
  }
}

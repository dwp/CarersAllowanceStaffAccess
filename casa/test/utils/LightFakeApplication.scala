package utils


import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeApplication
import services.mock.{AccessControlServiceStub, ClaimServiceStub}
import services.{AccessControlService, ClaimService}

/**
 * Created by peterwhitehead on 04/11/2015.
 */
class LightFakeApplication extends FakeApplication {
  val application = new GuiceApplicationBuilder().build

  override val injector = new GuiceApplicationBuilder()
    .bindings(bind[ClaimService].to[ClaimServiceStub])
    .bindings(bind[AccessControlService].to[AccessControlServiceStub])
    .injector
}

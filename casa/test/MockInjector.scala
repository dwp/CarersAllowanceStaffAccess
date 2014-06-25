import scala.language.existentials
import services.ClaimServiceStub
import utils.Injector

trait MockInjector extends Injector {

  import scala.reflect.{classTag, ClassTag}

  lazy val global = new CasaSettings with MockInjector {
    override def getControllerInstance[A](controllerClass: Class[A]): A = resolve(controllerClass)
  }

  override def resolve[A](clazz: Class[A]) = instances(clazz).asInstanceOf[A]

  private val instances: Map[Class[_ <: Any], Any] = {
    def controller[A: ClassTag](instance: A) = classTag[A].runtimeClass -> instance
    Map(controller[controllers.Application](new controllers.Application with ClaimServiceStub),
      controller[controllers.Auth](new controllers.Auth with AccessControlServiceMock))
  }
}

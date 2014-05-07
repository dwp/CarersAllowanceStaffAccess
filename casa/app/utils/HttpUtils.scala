package utils

import scala.concurrent.duration._
import play.api.libs.ws.{WS, Response}
import scala.concurrent.Await

object HttpUtils {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  class HttpMethodWrapper(url:String, timeout:FiniteDuration){
    def get[T](m:Response => T):T = Await.result(WS.url(url).get().map(m),timeout)

    def put[T](m:Response => T) = new {
      def exec(map:Map[String,Seq[String]] = Map.empty[String,Seq[String]]):T =  Await.result(WS.url(url).put(map).map(m),timeout)
    }

    def post[T](m:Response => T) = new {
      def exec(map:Map[String,Seq[String]] = Map.empty[String,Seq[String]]):T = Await.result(WS.url(url).post(map).map(m),timeout)
    }
  }
}

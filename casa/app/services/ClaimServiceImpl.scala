package services

import scala.concurrent.duration._
import org.joda.time.LocalDate
import play.api.libs.json._
import play.api.Play
import play.api.libs.ws.{Response, WS}
import org.joda.time.format.DateTimeFormat
import play.api.http.Status
import scala.concurrent.Await
import scala.language.postfixOps
import scala.language.implicitConversions
import play.api.libs.json.JsObject
import play.api.libs.json.JsArray
import scala.Some


object ClaimServiceImpl extends ClaimsService{

  val url = Play.configuration(Play.current).getString("claimsServiceUrl").getOrElse("http://localhost:9002")
  val timeout = Play.configuration(Play.current).getInt("ws.timeout").getOrElse(30).seconds

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  class HttpMethodWrapper(url:String){
    def get[T](m:Response => T):T = Await.result(WS.url(url).get().map(m(_)),timeout)

    def put[T](m:Response => T) = new {
      def exec(map:Map[String,Seq[String]] = Map.empty[String,Seq[String]]):T =  Await.result(WS.url(url).put(map).map(m(_)),timeout)
    }
  }

  implicit def stringGetWrapper(url: String) = new HttpMethodWrapper(url)

  override def claims(date: LocalDate): Option[JsArray] = {
    val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(date)

    s"$url/claims/$dateString" get { response =>
      response.status match {
        case Status.OK => Some(response.json.as[JsArray])
        case Status.BAD_REQUEST => None
      }
    }
  }

  override def claimsFiltered(date: LocalDate, status: String): Option[JsArray] = {

    val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(date)

    s"$url/claims/$dateString/$status" get { response =>
      response.status match {
        case Status.OK => Some(response.json.as[JsArray])
        case Status.BAD_REQUEST => None
      }
    }

  }

  override def claimNumbersFiltered(status: String*): JsObject =
    s"$url/counts/${status.mkString(",")}" get { response =>
      response.status match {
        case Status.OK => response.json.as[JsObject]
        case Status.BAD_REQUEST => Json.parse("{}").as[JsObject]
      }
    }


  override def updateClaim(transactionId: String, status: String): JsBoolean =
    s"$url/claim/$transactionId/$status" put { response =>
      response.status match {
        case Status.OK => JsBoolean(true)
        case Status.BAD_REQUEST => JsBoolean(false)
      }
    } exec()


  override def fullClaim(transactionId: String): Option[JsValue] =
    s"$url/claim/$transactionId/" get { response =>
      response.status match {
        case Status.OK => Some(response.json)
        case Status.BAD_REQUEST => None
      }
    }



}

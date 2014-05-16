package services

import scala.concurrent.duration._
import org.joda.time.LocalDate
import play.api.libs.json._
import play.api.{Logger, Play}
import org.joda.time.format.DateTimeFormat
import play.api.http.Status
import scala.language.postfixOps
import scala.language.implicitConversions
import play.api.libs.json.JsObject
import play.api.libs.json.JsArray
import scala.Some
import utils.HttpUtils.HttpMethodWrapper

object ClaimServiceImpl extends ClaimsService {

  val url = getUrl
  val timeout = Play.configuration(Play.current).getInt("ws.timeout").getOrElse(30).seconds

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  def getUrl = Play.configuration(Play.current).getString("claimsServiceUrl") match {
                case Some(s) if s.length > 0 => Logger.info(s"Getting claimServiceUrl value ($s)");s
                case _ => Logger.info("Getting default url value"); "http://localhost:9002"
  }

  implicit def stringGetWrapper(url: String) = new HttpMethodWrapper(url, timeout)

  override def claims(date: LocalDate): Option[JsArray] = {
    val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(date)

    s"$url/claims/$dateString" get { response =>
      response.status match {
        case Status.OK => Some(response.json.as[JsArray])
        case Status.NOT_FOUND => None
      }
    }
  }

  override def claimsFiltered(date: LocalDate, status: String): Option[JsArray] = {

    val dateString = DateTimeFormat.forPattern("ddMMyyyy").print(date)

    s"$url/claims/$dateString/$status" get { response =>
      response.status match {
        case Status.OK => Some(response.json.as[JsArray])
        case Status.NOT_FOUND => None
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
        case Status.OK => new JsBoolean(true)
        case Status.BAD_REQUEST => new JsBoolean(false)
      }
    } exec()

  override def fullClaim(transactionId: String): Option[JsValue] =
    s"$url/claim/$transactionId/" get { response =>
      response.status match {
        case Status.OK => Some(response.json)
        case Status.BAD_REQUEST => None
      }
    }

  override def renderClaim(transactionId: String) = {
    s"$url/render/$transactionId" get { response =>
      response.status match {
        case Status.OK =>
          val html = response.body
          Some(
            html.replace("<title></title>",s"<title>Claim PDF $transactionId</title>")
                .replace("##CHECK##","""<img src="/assets/img/yes.png" style="height:20px;"/>""")
                .replace("##CROSS##","""<img src="/assets/img/no.png" style="height:20px;"/>""")
                .replace("</body>","<script>window.onload = function(){window.opener.location.reload(false);};</script></body>")
                .replace("""<style type="text/css">""", styleSheetForHtmlOutput + """ <style type="text/css">""")

          )
        case Status.NOT_FOUND => None
      }
    }
  }

  def styleSheetForHtmlOutput = {
    """<link rel="stylesheet" type="text/css" href="/assets/stylesheets/claimHtmlOutput.css"/>
       <link rel="stylesheet" type="text/css" href="/assets/stylesheets/claimPrintHtmlOutput.css" media="print">
      <script src="http://code.jquery.com/jquery-1.9.1.min.js"></script><script>$(document).ready(function() {$('span').css('font-family','inherit').css('font-size','inherit').css('color', 'inherit').css('line-height', 'inherit');});</script>"""
  }
}

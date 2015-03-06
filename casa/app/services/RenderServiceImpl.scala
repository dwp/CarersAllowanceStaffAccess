package services

import monitoring.Counters
import play.api.http.Status
import play.api.{Logger, Play}
import utils.HttpUtils.HttpMethodWrapper

import app.ConfigProperties._

import scala.concurrent.duration._
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}


class RenderServiceImpl extends RenderService {
  override def claimHtml(xml: String) = RenderServiceImpl.html(xml)
}

object RenderServiceImpl {
  val renderServiceUrl = getUrl
  val timeout = getProperty("render.timeout",60).seconds // Play.configuration(Play.current).getInt("render.timeout").getOrElse(30).seconds

  def getUrl = {
    val url = getProperty("renderServiceUrl","http://localhost:9010")
    Logger.info(s"Using renderServiceUrl value ($url)");
    url
  }

  implicit def stringGetWrapper(url: String) = new HttpMethodWrapper(url,timeout)

  def call(xml:String) = {
    s"$renderServiceUrl/show" postXml { response =>
      response.status match {
        case Status.OK => response.body
        case _ =>
          Counters.incrementP1SubmissionErrorStatus(response.status)
          Logger.error(s"Submission to rendering service failed with status ${response.status}:${response.body}.")
          "Error"
      }
    } exec(xml)
  }

  def html(xml: String) = {

    Try(call(xml)) match {
      case Success(s) =>
        Some(s)
      case Failure(e) =>
        Counters.incrementP1SubmissionErrorStatus(0)
        Logger.error("Error while trying to connect to rendering service.",e)
        None
    }

  }
}

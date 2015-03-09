package services

import monitoring.Counters
import play.api.http.Status
import play.api.Logger

import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

/**
 * Implements call to Rendering Service to generate html. Relies on RenderServiceImpl object.
 */
class RenderServiceImpl extends RenderService {
  override def claimHtml(xml: String) = RenderServiceImpl.html(xml)
}

/**
 * Calls Rendering service to generate html.
 */
object RenderServiceImpl extends CasaRemoteService {

  override def getUrlPropertyName = "RenderingServiceUrl"

  override def getTimeoutPropertyName = "render.timeout"

  override def getDefaultUrl = "http://localhost:9010"

  private def call(xml:String) = {
    s"$url/show" postXml { response =>
      response.status match {
        case Status.OK => response.body
        case _ =>
          Counters.incrementP1SubmissionErrorStatus(response.status)
          Logger.error(s"Submission to rendering service failed with status ${response.status}:${response.body}.")
          "Error: Failed to render the claim."
      }
    } exec xml
  }

  /**
   * Call rendering service to convert an XML claim into an HTML claim.
   * @param xml claim to render into html.
   * @return Option containing html generated or None if problem occurred.
   */
  def html(xml: String) = {

    Try(call(xml)) match {
      case Success(s) =>
        Logger.debug("rendered successfully a claim.")
        Some(s)
      case Failure(e) =>
        Counters.incrementP1SubmissionErrorStatus(0)
        Logger.error("Error while trying to connect to rendering service.",e)
        None
    }

  }
}

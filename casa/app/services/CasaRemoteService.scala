package services

import utils.HttpWrapper

import scala.concurrent.duration._
import play.api.{Logger, Play}
import scala.language.implicitConversions

/**
 * Interface of remote services used by CASA.
 */
trait CasaRemoteService {

  def getUrlPropertyName: String

  def getDefaultUrl: String

  def getTimeoutPropertyName: String

  lazy val timeout = buildTimeout

  def buildTimeout: FiniteDuration = {
    Play.configuration(Play.current).getInt(getTimeoutPropertyName).getOrElse(30).seconds
  }

  lazy val url = buildUrl

  def buildUrl = Play.configuration(Play.current).getString(getUrlPropertyName) match {
    case Some(s) if s.length > 0 => Logger.info(s"Getting $getUrlPropertyName value ($s)"); s
    case _ => Logger.info("Getting default url value"); getDefaultUrl
  }

  //  implicit def stringGetWrapper(url: String) = new HttpMethodWrapper(url, timeout)
  implicit def stringGetWrapper(url: String) = {
    println("stringGetWrapper NO LONGER ACTIVE")
    val httpWrapper = new HttpWrapper
    httpWrapper.post(url, "", timeout.toMillis.toInt)
  }
}

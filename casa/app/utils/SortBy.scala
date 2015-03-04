package utils

import play.api.libs.json.JsArray

import utils.JsValueWrapper.improveJsValue
import scala.language.implicitConversions

/**
 * Created by valtechuk on 22/01/2015.
 */
object SortBy {


  def claimTypeDateTime(data: Option[JsArray]): Option[JsArray] = {
    data match {
      case Some(data) =>
        Some(JsArray(
          data.value.seq.sortWith(_.p.claimDateTime.asLong < _.p.claimDateTime.asLong)
            .sortWith(_.p.claimType.asType < _.p.claimType.asType)
        ))
      case _ => data
    }
  }

  def dateTime(d: Option[JsArray]): Option[JsArray] = {
    d match {
      case Some(data) =>
        Some(JsArray(
          data.value.seq.sortWith(_.p.claimDateTime.asLong < _.p.claimDateTime.asLong)
        ))
      case _ => d
    }
  }

  def surname(d: Option[JsArray]): Option[JsArray] = {
    d match {
      case Some(data) =>
        Some(JsArray(
          data.value.seq.sortWith((p1,p2)=>p1.p.surname.toString().compareToIgnoreCase(p2.p.surname.toString()) < 0)
        ))
      case _ => d
    }
  }

  def name(d: Option[JsArray]): Option[JsArray] = {
    d match {
      case Some(data) =>
        Some(JsArray(
          data.value.seq.sortWith((p1,p2)=>p1.p.forename.toString().compareToIgnoreCase(p2.p.forename.toString()) < 0)
        ))
      case _ => d
    }
  }


}

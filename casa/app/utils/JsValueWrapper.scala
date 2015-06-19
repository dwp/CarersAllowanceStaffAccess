package utils

import play.api.Logger
import play.api.libs.json.{JsUndefined, JsString, JsValue}
import org.joda.time.DateTime
import scala.language.dynamics
import scala.language.implicitConversions
import scala.util.{Success, Try}

object JsValueWrapper {

  implicit def improveJsValue(jsValue: JsValue) = new {
    private val improvedJsValue = new ImprovedJsValue(jsValue)
    def p = improvedJsValue
  }
}

class ImprovedJsValue(jsValue: JsValue) extends Dynamic {

  def selectDynamic(value:String) = {
    new ImprovedValue(
      jsValue \ value match {
        case undefined:JsUndefined=> JsString(s"$value is not defined")
        case jsValue:JsValue => jsValue
      }
    )
  }

}

class ImprovedValue(jsValue:JsValue) {

  def apply() = asString

  override def toString: String = asString

  def asString = jsValue.as[String]

  def asDate = jsValue.as[DateTime]

  def asInt = jsValue.as[Int]

  def asLong = jsValue.as[Long]

  def asType = if (asString == "claim") 1 else 2

}

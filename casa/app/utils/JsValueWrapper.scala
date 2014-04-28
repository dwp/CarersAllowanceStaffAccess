package utils

import play.api.libs.json.JsValue
import org.joda.time.DateTime
import scala.language.dynamics
import scala.language.implicitConversions

object JsValueWrapper {

  implicit def improveJsValue(jsValue: JsValue) = new {
    private val improvedJsValue = new ImprovedJsValue(jsValue)
    def p = improvedJsValue
  }
}

class ImprovedJsValue(jsValue: JsValue) extends Dynamic {

  def selectDynamic(value:String) = {
    new ImprovedValue(jsValue \ value)
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

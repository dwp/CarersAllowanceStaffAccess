import play.api.{Logger, Play}

import scala.util.{Success, Try}

package object app {

  object ConfigProperties  {
    def getIntProperty(property: String, throwError: Boolean = true): Int = getProperty(property, "Int", throwError).toInt

    def getStringProperty(property: String, throwError: Boolean = true): String = getProperty(property, "String", throwError).toString

    def getBooleanProperty(property: String, throwError: Boolean = true): Boolean = getProperty(property, "Boolean", throwError).toBoolean

    private def getProperty(property: String, propertyType: String, throwError: Boolean): String = {
      if (!throwError && Play.unsafeApplication == null) {
        defaultValue(propertyType)
      }
      else {
        (Play.current.configuration.getString(property), throwError) match {
          case (Some(s), _) => s.toString
          case (_, false) => defaultValue(propertyType)
          case (_, _) => {
            Logger.error("ERROR - getProperty failed to retrieve application property for:" + property)
            throw new IllegalArgumentException(s"ERROR - getProperty failed to retrieve application property for:$property")
          }
        }
      }
    }

    private def defaultValue(propertyType: String) = {
      propertyType match {
        case "String" => ""
        case "Int" => "-1"
        case "Boolean" => "false"
      }
    }
  }
}
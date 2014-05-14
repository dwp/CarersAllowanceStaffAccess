import net.litola.SassPlugin
import sbt._
import sbt.Keys._

object ApplicationBuild extends Build {

  val name = "a2"
  val version = "1.0-SNAPSHOT"

  val dependencies = Seq(
    "me.moocar"           % "logback-gelf"        % "0.9.6p2",
    "org.jasypt"          % "jasypt"              % "1.9.2"
  )

  var sO:Setting[_] = scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-Xlint", "-language:reflectiveCalls")

  var sV:Setting[_] = scalaVersion := "2.10.3"

  var sR:Seq[Setting[_]] = Seq(
    resolvers += "Carers repo" at "http://build.3cbeta.co.uk:8080/artifactory/repo/",
    resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases")

  var testOption:Setting[_] = javaOptions in Test += "-DclaimsServiceUrl="+(System.getProperty("claimsServiceUrl") match { case s:String => s case null => ""})

  val main = play.Project(name,version,dependencies).settings(SassPlugin.sassSettings ++ (sO +: sV +: testOption) ++ sR:_*)
}

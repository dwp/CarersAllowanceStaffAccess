import sbt._
import sbt.Keys._
import play.Play.autoImport._
import net.litola.SassPlugin

object ApplicationBuild extends Build {

  val name = "sa"
  val appVersion = "1.5-SNAPSHOT"

  val appDependencies = Seq(
    ws,
    "me.moocar"           % "logback-gelf"        % "0.12",
    "org.jasypt"          % "jasypt"              % "1.9.2",
    "com.dwp.carers"     %% "wscommons"           % "2.2",
    "com.dwp.carers"     %% "carerscommon"        % "6.4"
  )

  var sO:Setting[_] = scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-Xlint", "-language:reflectiveCalls")

  var sV:Setting[_] = scalaVersion := "2.10.4"

  var sR:Seq[Setting[_]] = Seq(
    resolvers += "Carers repo" at "http://build.3cbeta.co.uk:8080/artifactory/repo/",
    resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases")

  var jO: Seq[Def.Setting[_]] = Seq( javaOptions in Test += "-DclaimsServiceUrl="+(System.getProperty("claimsServiceUrl") match { case s:String => s case null => ""}),
javaOptions in Test += "-DaccessControlServiceUrl="+(System.getProperty("accessControlServiceUrl") match { case s:String => s case null => ""}))

  var vS: Seq[Def.Setting[_]] = Seq(version := appVersion, libraryDependencies ++= appDependencies)

  var appSettings: Seq[Def.Setting[_]] =  sV ++ sO ++ sR ++ jO ++ vS

  val main = Project(name, file(".")).enablePlugins(play.PlayScala, net.litola.SassPlugin).settings(appSettings: _*)
}

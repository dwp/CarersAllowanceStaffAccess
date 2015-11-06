
import sbt._
import sbt.Keys._
import play.sbt.PlayImport._

object ApplicationBuild extends Build {

  val name = "sa"
  val appVersion = "2.0-SNAPSHOT"

  val appDependencies = Seq(
    ws,
    filters,
    "me.moocar"           % "logback-gelf"        % "0.12",
    "org.jasypt"          % "jasypt"              % "1.9.2",
    "gov.dwp.carers"     %% "wscommons"           % "3.0",
    "gov.dwp.carers"     %% "carerscommon"        % "7.0",
    "org.specs2" %% "specs2-core" % "3.3.1" % "test" withSources() withJavadoc(),
    "org.specs2" %% "specs2-mock" % "3.3.1" % "test" withSources() withJavadoc(),
    "org.specs2" %% "specs2-junit" % "3.3.1" % "test" withSources() withJavadoc(),
    "com.kenshoo" % "metrics-play_2.10" % "2.4.0_0.4.0"
  )

  var sO:Setting[_] = scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-Xlint", "-language:reflectiveCalls")

  var sV:Setting[_] = scalaVersion := "2.10.5"

  var sR:Seq[Setting[_]] = Seq(
    resolvers += "Carers repo" at "http://build.3cbeta.co.uk:8080/artifactory/repo/",
    resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases")

  var jO: Seq[Def.Setting[_]] = Seq( javaOptions in Test += "-DclaimsServiceUrl="+(System.getProperty("claimsServiceUrl") match { case s:String => s case null => ""}),
javaOptions in Test += "-DaccessControlServiceUrl="+(System.getProperty("accessControlServiceUrl") match { case s:String => s case null => ""}))

  var vS: Seq[Def.Setting[_]] = Seq(version := appVersion, libraryDependencies ++= appDependencies)

  var appSettings: Seq[Def.Setting[_]] =  sV ++ sO ++ sR ++ jO ++ vS

  val main = Project(name, file(".")).enablePlugins(play.sbt.PlayScala).settings(appSettings: _*)
}

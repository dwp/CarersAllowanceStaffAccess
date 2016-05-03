
import sbt._
import sbt.Keys._
import play.sbt.PlayImport._
import play.sbt.routes.RoutesKeys._
import utils.ConfigurationChangeHelper._

object ApplicationBuild extends Build {
  val appName = "sa"
  val appVersion = "2.5-SNAPSHOT"

  processConfFiles(Seq("conf/application-info.conf"), Seq("application.version" -> appVersion, "application.name" -> appName))

  val appDependencies = Seq(
    ws,
    filters,
    "me.moocar"           % "logback-gelf"        % "0.12",
    "org.jasypt"          % "jasypt"              % "1.9.2",
    "gov.dwp.carers"     %% "carerscommon"        % "7.15-SNAPSHOT",
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
    resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"
  )

  var jO: Seq[Def.Setting[_]] = Seq( javaOptions in Test += "-DclaimsServiceUrl="+(System.getProperty("claimsServiceUrl") match { case s:String => s case null => ""}),
  javaOptions in Test += "-DaccessControlServiceUrl="+(System.getProperty("accessControlServiceUrl") match { case s:String => s case null => ""}))

  var vS: Seq[Def.Setting[_]] = Seq(routesGenerator := InjectedRoutesGenerator, libraryDependencies ++= appDependencies)

  var sAppN: Seq[Def.Setting[_]] = Seq(name := appName)
  var sAppV: Seq[Def.Setting[_]] = Seq(version := appVersion)
  var sOrg: Seq[Def.Setting[_]] = Seq(organization := "gov.dwp.carers")

  val isSnapshotBuild = appVersion.endsWith("-SNAPSHOT")
  var publ: Seq[Def.Setting[_]] = Seq(
    publishTo := Some("Artifactory Realm" at "http://build.3cbeta.co.uk:8080/artifactory/repo/"),
    publishTo <<= version {
      (v: String) =>
        if (isSnapshotBuild)
          Some("snapshots" at "http://build.3cbeta.co.uk:8080/artifactory/libs-snapshot-local")
        else
          Some("releases" at "http://build.3cbeta.co.uk:8080/artifactory/libs-release-local")
    })

  var appSettings: Seq[Def.Setting[_]] =  sV ++ sO ++ sR ++ jO ++ vS ++ sAppN ++ sAppV ++ sOrg ++ publ

  val main = Project(appName, file(".")).enablePlugins(play.sbt.PlayScala).settings(appSettings: _*)
}

import net.litola.SassPlugin
import sbt._
import sbt.Keys._

object ApplicationBuild extends Build {

  val name = "casa"
  val version = "1.0-SNAPSHOT"

  val dependencies = Seq(

  )

  var sO:Setting[_] = scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-Xlint", "-language:reflectiveCalls")

  var sV:Setting[_] = scalaVersion := "2.10.3"

  var sR:Seq[Setting[_]] = Seq(
    resolvers += "Carers repo" at "http://build.3cbeta.co.uk:8080/artifactory/repo/",
    resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases")


  val main = play.Project(name,version,dependencies).settings(SassPlugin.sassSettings ++ (sO +: sV) ++ sR:_*)
}
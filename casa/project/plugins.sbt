// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += "Carers repo" at "http://build.3cbeta.co.uk:8080/artifactory/repo/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.3")

addSbtPlugin("default" % "sbt-sass" % "0.1.9")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.1.6")

libraryDependencies += "gov.dwp.carers" %% "carerscommon" % "7.7"


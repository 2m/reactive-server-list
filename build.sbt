organization := "2m"

name := "reactive-server-list"

version := "0.0.3-SNAPSHOT"

scalaVersion := "2.11.2"

libraryDependencies ++= {
  val akkaVersion = "2.3.4"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
    "net.sourceforge.queried" % "queried" % "2.7.0",
    "com.typesafe.slick" %% "slick" % "2.1.0",
    "com.h2database" % "h2" % "1.4.179",
    "org.scalatest" %% "scalatest" % "2.2.1" % "test",
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
  )
}

publishTo := Some("Bintray API Realm" at "https://api.bintray.com/maven/2m/maven/reactive-server-list")

organization := "2m"

name := "reactive-server-list"

version := "0.0.2"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.3",
  "com.typesafe.akka" %% "akka-contrib" % "2.3.3",
  "net.sourceforge.queried" % "queried" % "2.7.0",
  "org.scalatest" % "scalatest_2.11" % "2.1.5" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.3" % "test"
)

name := "reactive-server-list"

version := "1.0"

scalaVersion := "2.11.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.2",
  "net.sourceforge.queried" % "queried" % "2.7.0",
  "org.scalatest" % "scalatest_2.11" % "2.1.5" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.2" % "test"
)

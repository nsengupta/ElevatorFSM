name := """akka-sample-fsm-scala"""

version := "2.4"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  // https://mvnrepository.com/artifact/com.typesafe.akka/akka-actor_2.11
  "com.typesafe.akka" % "akka-actor_2.11" % "2.4.12",
// https://mvnrepository.com/artifact/org.scalatest/scalatest_2.11
  "org.scalatest" % "scalatest_2.11" % "2.1.3",
  // https://mvnrepository.com/artifact/com.typesafe.akka/akka-testkit_2.11,
  "com.typesafe.akka" % "akka-testkit_2.11" % "2.3.15"
)

libraryDependencies += "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.6"




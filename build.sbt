name := """distributed hashmap"""

version := "2.4.11"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.11",
  "com.typesafe.akka" %% "akka-remote" % "2.4.11"
)

licenses := Seq(("CC0", url("http://creativecommons.org/publicdomain/zero/1.0")))


fork in run := true

javaOptions += "-Xmx4G"
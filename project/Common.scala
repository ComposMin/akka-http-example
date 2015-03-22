import sbt._
import sbt.Keys._

object Common {

  val settings =
    List(
      // Core settings
      organization := "net.composmin.poc",
      version := "1.0.0",
      scalaVersion := Version.scala,
      crossScalaVersions := List(scalaVersion.value),
      scalacOptions ++= List(
        "-unchecked",
        "-deprecation",
        "-language:_",
        "-target:jvm-1.6",
        "-encoding", "UTF-8"
      ),
      unmanagedSourceDirectories in Compile := List((scalaSource in Compile).value),
      unmanagedSourceDirectories in Test := List((scalaSource in Test).value),
      resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      libraryDependencies ++= Dependencies.restapi
    )
    
}

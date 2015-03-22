import sbt._

object Version {
  val akka         = "2.3.9"
  val logback      = "1.1.2"
  val scala        = "2.11.5"
  val scalaParsers = "1.0.3"
  val scalaTest    = "2.2.4"


  val httpCore     = "1.0-M4"
  val akkaStream   = "1.0-M4"
}

object Library {
  val akkaActor       = "com.typesafe.akka"      %% "akka-actor"                    % Version.akka
  val akkaContrib     = "com.typesafe.akka"      %% "akka-contrib"                  % Version.akka
  val akkaSlf4j       = "com.typesafe.akka"      %% "akka-slf4j"                    % Version.akka
  val akkaTestkit     = "com.typesafe.akka"      %% "akka-testkit"                  % Version.akka
  val logbackClassic  = "ch.qos.logback"         %  "logback-classic"               % Version.logback
  val scalaParsers    = "org.scala-lang.modules" %% "scala-parser-combinators"      % Version.scalaParsers
  val scalaTest       = "org.scalatest"          %% "scalatest"                     % Version.scalaTest

  val httpCore        = "com.typesafe.akka"      %% "akka-http-core-experimental"   % Version.httpCore
  val akkaStream      = "com.typesafe.akka"      %% "akka-stream-experimental"      % Version.akkaStream
}

object Dependencies {

  import Library._

  val restapi = List(
    akkaContrib,
    akkaSlf4j,
    httpCore,
    akkaStream,
    logbackClassic,
    scalaParsers,
    akkaTestkit % "test",
    scalaTest   % "test"
  )
}

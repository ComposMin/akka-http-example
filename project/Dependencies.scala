import sbt._

object Version {
  val akka         = "2.4.4"
  val logback      = "1.1.6"
  val scala        = "2.11.8"
  val scalaParsers = "1.0.4"
  val scalaTest    = "2.2.6"
}

object Library {
  val akkaActor       = "com.typesafe.akka"      %% "akka-actor"                    % Version.akka
  val akkaContrib     = "com.typesafe.akka"      %% "akka-contrib"                  % Version.akka
  val akkaSlf4j       = "com.typesafe.akka"      %% "akka-slf4j"                    % Version.akka
  val akkaTestkit     = "com.typesafe.akka"      %% "akka-testkit"                  % Version.akka
  val httpCore        = "com.typesafe.akka"      %% "akka-http-core"                % Version.akka
  val httpExperimental= "com.typesafe.akka"      %% "akka-http-experimental"   	    % Version.akka
  val akkaStream      = "com.typesafe.akka"      %% "akka-stream"                   % Version.akka
  val logbackClassic  = "ch.qos.logback"         %  "logback-classic"               % Version.logback
  val scalaParsers    = "org.scala-lang.modules" %% "scala-parser-combinators"      % Version.scalaParsers
  val scalaTest       = "org.scalatest"          %% "scalatest"                     % Version.scalaTest

}

object Dependencies {

  import Library._

  val restapi = List(
    akkaContrib,
    akkaSlf4j,
    httpCore,
    httpExperimental,
    akkaStream,
    logbackClassic,
    scalaParsers,
    akkaTestkit % "test",
    scalaTest   % "test"
  )
}

package net.composmin.akkahttp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout

import scala.concurrent.duration._

object Main extends App {
  implicit val system = ActorSystem("main-system")
  implicit val materializer = ActorMaterializer()
  implicit val askTimeout = Timeout(3.seconds)
  implicit val executionContext = system.dispatcher

  val staticFiles: Route =
    getFromResourceDirectory("web") ~
      path("")(getFromResource("web/index.html"))

  val status: Route =
    path("status")(complete( """{ "status" : "ok" }"""))

  val bindingFuture = Http().bindAndHandle(staticFiles ~ status, "0.0.0.0", 8080)

  StreamExperiment.runServer(system, 8081)

  println(s"Server online at http://localhost:8080/status\n")
}

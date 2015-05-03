
import akka.actor.{ActorSystem, Props}
import akka.http.Http
import akka.http.server.Directives._
import akka.http.server.Route
import akka.stream.{ActorFlowMaterializer, FlowMaterializer}
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Main extends App {
  implicit val system = ActorSystem("main-system")

  implicit val materializer = ActorFlowMaterializer()
  implicit val askTimeout = Timeout(3.seconds)

  val delayer = system.actorOf(Props[RequestDelayingActor])

  import system.dispatcher

  Http(system).bindAndHandle(staticFiles ~ status(), "0.0.0.0", 8080)


  private def staticFiles(): Route =
    getFromResourceDirectory("web") ~
      path("")(getFromResource("web/index.html"))

  private def status() : Route =
    path("status")(complete( """{ "status" : "ok" }"""))

}

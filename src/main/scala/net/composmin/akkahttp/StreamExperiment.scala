package net.composmin.akkahttp

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.{IncomingConnection, ServerBinding}
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream._
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Source, Zip}
import akka.util.Timeout
import akka.{Done, NotUsed}

import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, _}

/**
  * FIXME : All code in this file is currently inactive.
  * It demonstrates the use of HTTP flows to functionally transform input (HTTPRequest) to output (HTTPResponse).
  */
object StreamExperiment {

  import Routes._

  def runServer(implicit actSys: ActorSystem, port: Int): Future[Done] = {
    implicit val materializer = ActorMaterializer()

    val server: Source[IncomingConnection, Future[ServerBinding]] = Http().bind(interface = "0.0.0.0", port)

    server.runForeach { (connection: IncomingConnection) =>

      val handler = Flow[HttpRequest].via(delayMatched(predicate)).map(Routes.requestHandler)

      connection.handleWith(handler)
    }
  }

}


class RequestDelayingActor extends Actor {
  implicit val system = context.system
  implicit val ec = system

  case class DelayWrapper(replyTo: ActorRef, req: HttpRequest)

  override def receive: Receive = {
    case req@HttpRequest(GET, Uri.Path("/delayed"), _, _, _) ⇒
      import scala.concurrent.ExecutionContext.Implicits.global
      context.system.scheduler.scheduleOnce(2 second, self, DelayWrapper(sender(), req))

    case wrap: DelayWrapper =>
      sender ! HttpResponse(entity = "PONG!")

    case req: HttpRequest ⇒ sender ! Routes.requestHandler(req)
  }
}


object Routes {

  val requestHandler: HttpRequest ⇒ HttpResponse = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) ⇒
      HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, "<html><body>Hello world!</body></html>"))

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) ⇒ HttpResponse(entity = "PONG!")

    case HttpRequest(GET, Uri.Path("/blah"), _, _, _) ⇒ HttpResponse(entity = "PONG!")

    case HttpRequest(GET, Uri.Path("/crash"), _, _, _) ⇒ sys.error("BOOM!")

    case _: HttpRequest ⇒ HttpResponse(404, entity = "Unknown resource!")
  }

  def asyncRequestHandler(system: ActorSystem): HttpRequest => Future[HttpResponse] = {
    req => {
      import akka.pattern.ask
      implicit val askTimeout = Timeout(3 seconds)

      val a: ActorRef = system.actorOf(Props[RequestDelayingActor])

      (a ? req).mapTo[HttpResponse]
    }
  }

  def predicate(httpRequest: HttpRequest): Boolean = {
    httpRequest match {
      case HttpRequest(GET, Uri.Path("/ping"), _, _, _) ⇒ true
      case _ => false
    }
  }

  def throttle[T](rate: FiniteDuration): Graph[FlowShape[T, T], NotUsed] = {
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val zip = builder.add(Zip[T, Unit.type]())
      Source.tick(rate, rate, Unit) ~> zip.in1

      val ignoreTick = zip.out.map(_._1)
      FlowShape(zip.in0, ignoreTick.outlet)
    }
  }

  def delayMatched[T](p: T => Boolean): Graph[FlowShape[T, T], NotUsed] = {
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val split = builder.add(Flipper(p))
      val slowLane = builder.add(throttle[T](600 millis))
      val merge = builder.add(Merge[T](2))

      split.out0 ~> slowLane ~> merge.in(0)

      split.out1 ~> merge.in(1)

      FlowShape(split.in, merge.out)
    }
  }

}

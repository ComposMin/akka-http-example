import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.model.HttpMethods._
import akka.http.model._
import akka.stream.scaladsl.{Flow, Merge, Source, Zip}
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, _}

/**
 * FIXME : All code in this file is currently inactive.
 * It demonstrates the use of HTTP flows to functionally transform input (HTTPRequest) to output (HTTPResponse).
 */



class StreamExperiment {
//  val server: Source[IncomingConnection, Future[ServerBinding]] = Http(system).bind(interface = "0.0.0.0", port = 8080)
//
//  server.runForeach { (connection: IncomingConnection) =>
//
//    val handlerFlow = Flow[HttpRequest].via(delayMatched(predicate)).map(Routes.requestHandler)
//
//    connection.handleWith(handlerFlow)
//  }
//
}



class RequestDelayingActor extends Actor {
  implicit val system = context.system
  implicit val ec = system

  case class DelayWrapper(replyTo: ActorRef, req: HttpRequest)

  override def receive: Receive = {
    case req @ HttpRequest(GET, Uri.Path("/delayed"), _, _, _) ⇒ {
      import scala.concurrent.ExecutionContext.Implicits.global
      context.system.scheduler.scheduleOnce(2 second, self, DelayWrapper(sender(), req))
    }
    case wrap: DelayWrapper => {
      sender ! HttpResponse(entity = "PONG!")
    }
    case req: HttpRequest ⇒ sender ! Routes.requestHandler(req)
  }
}



object Routes {

  val requestHandler: HttpRequest ⇒ HttpResponse = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) ⇒
      HttpResponse(
        entity = HttpEntity(MediaTypes.`text/html`,
          "<html><body>Hello world!</body></html>"))

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) ⇒ {
      HttpResponse(entity = "PONG!")
    }
    case HttpRequest(GET, Uri.Path("/blah"), _, _, _) ⇒ {
      HttpResponse(entity = "PONG!")
    }
    case HttpRequest(GET, Uri.Path("/crash"), _, _, _) ⇒ sys.error("BOOM!")
    case _: HttpRequest                                ⇒ HttpResponse(404, entity = "Unknown resource!")
  }

  def asyncRequestHandler(system: ActorSystem): HttpRequest => Future[HttpResponse] = {
    req =>
    {
      import akka.pattern.ask
      implicit val askTimeout = Timeout(3 seconds)

      val a: ActorRef = system.actorOf(Props[RequestDelayingActor])

      (a ? req).mapTo[HttpResponse]
    }
  }

  def predicate(httpRequest: HttpRequest): Boolean = {
    httpRequest match {
      case HttpRequest(GET, Uri.Path("/ping"), _, _, _) ⇒ true
      case _                                            => false
    }
  }

  def throttle[T](rate: FiniteDuration): Flow[T, T, Unit] = {
    Flow() { implicit builder =>
      import akka.stream.scaladsl.FlowGraph.Implicits._
      val zip = builder.add(Zip[T, Unit.type]())
      Source(rate, rate, Unit) ~> zip.in1
      (zip.in0, zip.out)
    }.map(_._1)
  }

  def delayMatched[T](p: T => Boolean): Flow[T, T, Unit] = {
    Flow() { implicit builder =>
      import akka.stream.scaladsl.FlowGraph.Implicits._

      val split = builder.add(Flipper(p))
      val slowLane = builder.add(throttle[T](600 millis))
      val merge = builder.add(Merge[T](2))

      split.out0 ~> slowLane ~> merge.in(0)

      split.out1 ~> merge.in(1)

      (split.in, merge.out)
    }
  }

}

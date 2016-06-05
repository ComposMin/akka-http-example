package net.composmin.akkahttp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.{IncomingConnection, ServerBinding}
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream._
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Source, Zip}
import akka.{Done, NotUsed}

import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, _}

/**
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


object Routes {

  val requestHandler: HttpRequest ⇒ HttpResponse = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) ⇒
      HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, "<html><body>Hello world!</body></html>"))

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) ⇒ HttpResponse(entity = "PONG!")

    case HttpRequest(GET, Uri.Path("/blah"), _, _, _) ⇒ HttpResponse(entity = "PONG!")

    case HttpRequest(GET, Uri.Path("/crash"), _, _, _) ⇒ sys.error("BOOM!")

    case _: HttpRequest ⇒ HttpResponse(404, entity = "Unknown resource!")
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


import akka.stream._
import akka.stream.scaladsl.{ Broadcast, FlowGraph, Flow }

/**
 * A custom Graph section that takes a single input and chooses which of its two outputs to
 * send it to based on a supplied predicate. NOTE: out0 is the output used when the input matches the predicate.
 */
object Flipper {

  def apply[In](p: In => Boolean): Graph[FanOutShape2[In, In, In], Unit] = {
    FlowGraph.partial() { implicit b ⇒
      import FlowGraph.Implicits._

      // TODO: This code evaluates the predicate twice, which could be a performance problem if
      // the predicate is expensive to calculate. An improved implementation could evaluate once
      // and then pass the pair down the flow and then emit the result based on the boolean in the pair
      // OR
      // use a FlexiRoute primitive and determine which output to route to directly.
      val bcast = b.add(Broadcast[In](2))
      val matching = b.add(Flow[In].filter(p))
      val nonMatching = b.add(Flow[In].filter(x => !p(x)))

      bcast.out(0) ~> matching
      bcast.out(1) ~> nonMatching

      new FanOutShape2[In, In, In](bcast.in, matching.outlet, nonMatching.outlet)
    }
  }
}
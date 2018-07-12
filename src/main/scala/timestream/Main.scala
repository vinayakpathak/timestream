package timestream

import breeze.linalg.linspace
import breeze.plot.{Figure, plot}
import cats.effect.IO
import fs2.{Pure, Stream}

object Main extends App {
  import Timed._
  import Smoothing._

  def ask(prompt: String): String = {
    print(s"$prompt> ")
    scala.io.StdIn.readLine()
  }

  val in1: TimedStream[IO, Double] = Stream.repeatEval(IO(ask("stream").toDouble)).map(now).take(5)
  val in2: TimedStream[Pure, Double] = Stream(now(1.0), now(2.12), now(5.12), now(10.33))
  val in3: TimedStream[IO, Double] = Simulate.brownianMotion(now(0), 1, 0.1).take(20)

  //  val out = in3.observe1(td => IO(println(("in", td))))
  //    .through[Timed[Double]](ema(0.1))
  //    .observe1(td => IO(println(("out", td))))
  //    .compile.toVector.unsafeRunAsync(_ => ())

  val out = in2.through[Timed[Double]](ema(0.1)).toList
  println(out)
  val f = Figure()
  val p = f.subplot(0)
  val x = linspace(0.0,1.0)
  p += plot(x, x :^ 2.0)
  p += plot(x, x :^ 3.0, '.')
  p.xlabel = "x axis"
  p.ylabel = "y axis"
  f.saveas("lines.png")



  //  val changingNum = async.signalOf[IO, Double](0.0).unsafeRunSync()
  //  val changingNumStream = changingNum.discrete.take(10).observe1(cn => IO{Thread.sleep(2000); println(cn)}).compile.drain.unsafeRunAsync(_ => ())
  //
  //  Stream.every(1.second).filter(x => x).zipWithIndex.observe1(p => changingNum.set(p._2)).compile.drain.unsafeRunAsync(_ => ())




  //  println(Stream(1, 2, 3, 4).flatMap(i => Stream.constant(i)).take(10).toList)
  //  Stream(1, 2, 3, 4).flatMap(i => Stream.repeatEval(IO(ask(s"stream$i")))).take(10).compile.drain.unsafeRunAsync(_ => ())

}

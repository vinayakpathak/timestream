package com.vinpat.timestream

import java.time.temporal.ChronoUnit
import java.time.{Duration, LocalDateTime}

import fs2._
import cats._
import cats.syntax.functor._
import cats.effect._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._
import breeze.stats.distributions._
import breeze.plot._

case class Timed[A](value: A, dateTime: LocalDateTime) {
  def <->(that: Timed[A]) = {
    if (that.dateTime.isEqual(LocalDateTime.MIN)) {
      Double.PositiveInfinity
    }
    else {
      that.dateTime.until(this.dateTime, ChronoUnit.MILLIS).toDouble
    }
  }
}

object Timed {
  type TimedStream[F[_], A] = Stream[F, Timed[A]]

  implicit val timedFunctor: Functor[Timed] = new Functor[Timed] {
    def map[A, B](ta: Timed[A])(f: A => B): Timed[B] = ta.copy(value = f(ta.value))
  }

  def now[A](a: A) = Timed(a, LocalDateTime.now)

  def atMinTime[A](a: A) = Timed(a, LocalDateTime.MIN)
}

object Smoothing {
  import Timed._
  def ema[F[_]](lambda: Double): Pipe[F, Timed[Double], Timed[Double]] = s => s.scan[Timed[Double]](atMinTime(0)){(avg, td) =>
    val timeDiff = td <-> avg
    val lambdaPerMilli = lambda/1000
    val newAvg = avg.value * math.exp(-lambdaPerMilli*timeDiff) + td.value * (1 - math.exp(-lambdaPerMilli*timeDiff))
    td.copy(value = newAvg)
  }
}

object Simulate {
  import com.vinpat.timestream.Timed._
  def brownianMotion(start: Timed[Double], sigma: Double, lambda: Double): TimedStream[IO, Double] = {
    val expo = new Exponential(lambda)
    val gaus = new Gaussian(0, sigma)
    Stream.unfoldEval[IO, Timed[Double], Timed[Double]](start)(td => IO{
      val interv = expo.sample().toLong
      val t = td.dateTime.plusSeconds(interv)
      val v = td.value + gaus.sample() * math.sqrt(interv.toDouble)
      val ntd = Timed(v, t)
      Some((ntd, ntd))
    })
  }
}

object Main extends App {
  import Timed._
  import Smoothing._

  def ask(prompt: String): String = {
    print(s"$prompt> ")
    scala.io.StdIn.readLine()
  }

//  val in1: TimedStream[IO, Double] = Stream.repeatEval(IO(ask("stream").toDouble)).map(now).take(5)
//  val in2: TimedStream[Pure, Double] = Stream(now(1.0), now(2.12), now(5.12), now(10.33))
//  val in3: TimedStream[IO, Double] = Simulate.brownianMotion(now(0), 1, 0.1).take(20)
//
//  val out = in1.observe1(td => IO(println(("in", td))))
//    .through[Timed[Double]](ema(0.1))
//    .observe1(td => IO(println(("out", td))))
//    .compile.toVector.unsafeRunAsync(_ => ())





  val changingNum = async.signalOf[IO, Double](0.0).unsafeRunSync()
  val changingNumStream = changingNum.discrete.take(10).observe1(cn => IO{Thread.sleep(2000); println(cn)}).compile.drain.unsafeRunAsync(_ => ())

  Stream.every(1.second).filter(x => x).zipWithIndex.observe1(p => changingNum.set(p._2)).compile.drain.unsafeRunAsync(_ => ())




//  println(Stream(1, 2, 3, 4).flatMap(i => Stream.constant(i)).take(10).toList)
//  Stream(1, 2, 3, 4).flatMap(i => Stream.repeatEval(IO(ask(s"stream$i")))).take(10).compile.drain.unsafeRunAsync(_ => ())

}

//todo: Look into continuous streams. Depth an application? Maintaining an estimate an application?
//todo: Applications of interleave?
//todo: We don't need parallelism for repeatEval. Or for observe1 because it's all pull based.

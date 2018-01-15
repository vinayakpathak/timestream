package com.vinpat.timestream

import java.time.temporal.ChronoUnit
import java.time.{Duration, LocalDateTime}

import fs2._
import cats._
import cats.syntax.functor._
import cats.effect._

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


object Main extends App {
  import Timed._
  import Smoothing._

  val in1: TimedStream[IO, Double] = Stream.repeatEval(IO(scala.io.StdIn.readLine().toDouble)).map(now)
  val in2: TimedStream[Pure, Double] = Stream(now(1.0), now(2.12), now(5.12), now(10.33))

  val out = in1
    .through[Timed[Double]](ema(0.1))
    .observe1(td => IO(println(td)))
  out.compile.toVector.unsafeRunAsync(e => println(e))
}
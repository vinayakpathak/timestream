package com.vinpat.timestream

import java.time.LocalDateTime
import fs2._
import cats._
import cats.syntax.functor._

case class Timed[A](value: A, dateTime: LocalDateTime)

object Timed {
  type TimedStream[F[_], A] = Stream[F, Timed[A]]

  implicit val timedFunctor: Functor[Timed] = new Functor[Timed] {
    def map[A, B](ta: Timed[A])(f: A => B): Timed[B] = ta.copy(value = f(ta.value))
  }

  def now[A](a: A) = Timed(a, LocalDateTime.now)
}

object Smoothing {
  import Timed._
  def ema[F[_]]: Pipe[F, Timed[Double], Timed[Double]] = s => s.map(td => td.map(_ + 1)) //stub implementation
}

object Main extends App {
  import Timed._
  import Smoothing._

  val in1: TimedStream[Task, Double] = Stream.repeatEval(Task.delay(scala.io.StdIn.readLine().toDouble)).map(now)
  val in2: TimedStream[Nothing, Double] = Stream(now(1.0), now(2.12), now(5.12), now(10.33))

  val out = in1
    .through[Timed[Double]](ema)
    .evalMap[Task, Task, Unit](td => Task.now(println(td)))
  out.runLog.unsafeRun()
}
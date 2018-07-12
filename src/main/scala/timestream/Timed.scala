package timestream

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
import breeze.linalg._

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
  type TimedDist = Timed[Rand[DenseVector[Double]]]

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


//todo: We don't need parallelism for repeatEval. Or for observe1 because it's all pull based.
//todo: Applications of interleave?
//todo: Look into continuous streams. Depth an application? Maintaining an estimate an application?

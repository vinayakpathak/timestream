package timestream

import fs2._
import cats._
import cats.syntax.functor._
import cats.effect._
import breeze.stats.distributions._

object Simulate {
  import Timed._
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

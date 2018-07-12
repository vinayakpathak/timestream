package timestream.bayes

import breeze.linalg.DenseVector
import breeze.stats.distributions.Rand

object Bayes {
  type Distribution = Rand[DenseVector[Double]]
  trait Model[S, T] {
    def likelihood: S => Rand[T]

    def update[T](prior: Rand[S], evidence: T): Rand[S] = {
      val samples = prior.samplesVector(100)
      ???
    }

    def updateConjugate[T, Prior <: Rand[S], Likelihood <: Rand[T]]
  }

  implicit class ProbOps[S](prior: Rand[S]) {
    def update[T](evidence: T)(implicit m: Model[S, T]): Rand[S]
  }

}

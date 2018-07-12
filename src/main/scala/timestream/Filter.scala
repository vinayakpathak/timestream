package timestream

import breeze.linalg.DenseVector
import breeze.stats.distributions.Rand
import fs2._
import timestream.Timed.TimedDist

trait Filter {
  def filter[F[_]]: Pipe[F, TimedDist, TimedDist] = {

  }
}

package com.vinpat.timestream

import java.time.LocalDateTime

case class Timed[A](value: A, dateTime: LocalDateTime)

object Timed {
  type TimedStream[F[_], A] = Stream[F, Timed[A]]
}
package com.nubank.assignment.producer

import cats.effect.{Sync, Temporal}
import fs2.Stream

import scala.io.StdIn

trait Listener[F[_]] {
  def listen: Stream[F, String]
  def subscribe: F[Unit]
}

object Listener {
  def make[F[_]: Sync: Temporal](p: Producer[F, String]): F[Listener[F]] = Sync[F].pure {
    new Listener[F] {
      override def listen: Stream[F, String] = Stream.repeatEval(Sync[F].delay(StdIn.readLine)).filter(line => line != null && line.nonEmpty)
      override def subscribe: F[Unit]        = listen.evalMap(p.send).compile.drain
    }
  }
}
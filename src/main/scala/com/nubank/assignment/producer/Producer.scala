package com.nubank.assignment.producer

import cats.effect.Sync
import cats.effect.std.Queue

sealed trait Producer[F[_], -T] {
  def send(message: T): F[Unit]
}

object Producer {
  def console[F[_]: Sync](q: Queue[F, String]): F[Producer[F, String]] = Sync[F].pure(new ConsoleProducer[F](q))
}

final class ConsoleProducer[F[_] : Sync] (q: Queue[F, String]) extends Producer[F, String] {
  override def send(message: String): F[Unit] = q.offer(message)
}

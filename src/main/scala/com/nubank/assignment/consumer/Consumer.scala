package com.nubank.assignment.consumer

import cats.Functor
import cats.effect.Sync
import cats.effect.std.Queue
import fs2.Stream
import io.circe.Decoder
import io.circe.parser.decode

sealed trait Consumer[S[_], T] {
  def subscribe: S[T]
}

trait StreamConsumer[F[_], T] extends Consumer[Stream[F, *], T]

object Consumer {
  def make[F[_]: Sync, T: Decoder](q: Queue[F, String]): F[StreamConsumer[F, T]] = Sync[F].pure(new MessageConsumer(q))
}

final class MessageConsumer[F[_] : Functor, T: Decoder] (queue: Queue[F, String]) extends StreamConsumer[F, T] {
  override def subscribe: Stream[F, T] = Stream.fromQueueUnterminated(queue).map {
    json =>
      decode[T](json) match {
        case Left(err) =>
          println(s"MessageConsumer: $err")
          None
        case Right(message) =>
          Some(message)
      }
  }.unNone
}
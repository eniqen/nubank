package com.nubank.assignment

import _root_.fs2._
import cats.effect.std.Queue
import cats.effect._
import com.nubank.assignment.consumer.Consumer
import com.nubank.assignment.producer.{Listener, Producer}
import com.nubank.assignment.sink.Sink

import scala.concurrent.duration._

object AuthorizerApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = start[IO].flatMap(_ => IO.never).as(ExitCode.Success)

  def start[F[_]: Async: Temporal]: F[Unit] = (for {
    queue       <- Stream.eval(Queue.bounded[F, String](1000))
    producer    <- Stream.eval(Producer.console[F](queue))
    consumer    <- Stream.eval(Consumer.make[F, InputMessage](queue)) concurrently Stream.eval(Listener.make[F](producer)).evalTap(_.subscribe)
    fileSink    <- Stream.eval(Sink.console[F])
    _           <- Stream.eval(Authorizer.make(consumer, fileSink)).evalTap(_.process(10, 10.millis))
  } yield ()).compile.drain
}
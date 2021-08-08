package com.nubank.assignment.sink

import java.nio.file.Path
import java.nio.file.StandardOpenOption.{APPEND, CREATE}

import cats.effect.{Async, Sync}
import com.nubank.assignment.logger
import fs2.io.file.Files
import fs2.{Chunk, Stream, text}

sealed trait Sink[F[_]] {
  def batch(m: Chunk[String]): F[Unit]
}

object Sink {
  def file[F[_]: Async](path: Path): F[FileSink[F]] = Sync[F].pure(new FileSink[F](path))
  def console[F[_]: Sync]: F[Sink[F]]               = Sync[F].pure(new ConsoleSink[F]())
}

final class FileSink[F[_] : Async] (path: Path) extends Sink[F] {
  val Separator = "\n"

  override def batch(m: Chunk[String]): F[Unit] =
    Stream
      .chunk(m)
      .covary[F]
      .intersperse(Separator)
      .through(text.utf8Encode)
      .through(Files[F].writeAll(path, Seq(CREATE, APPEND)))
      .intersperse(Separator)
      .compile
      .drain
}

final class ConsoleSink[F[_]: Sync]() extends Sink[F] {
  override def batch(m: Chunk[String]): F[Unit] = Stream.chunk(m).through(logger).compile.drain
}



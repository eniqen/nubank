package com.nubank.assignment

import cats.effect.{Sync, Temporal}
import cats.implicits.catsSyntaxOptionId
import com.nubank.assignment.InputMessage.{AccountInfo, TransactionInfo}
import com.nubank.assignment.consumer.StreamConsumer
import com.nubank.assignment.sink.Sink
import com.nubank.assignment.validation.{AccountRules, TransactionRules}
import fs2.Stream
import io.circe.syntax.EncoderOps

import scala.concurrent.duration.FiniteDuration

final class Authorizer[F[_]: Sync: Temporal] private[Authorizer] (consumer: StreamConsumer[F, InputMessage], sink: Sink[F]) {
  def authorize: Stream[F, OutputMessage] =
    consumer.subscribe.mapAccumulate(Map.empty[Int, AccountState]) {
      case (state, acc: AccountInfo) =>
        AccountRules.checkAll(state.get(0))(acc).fold(
          violation    =>
            state -> OutputMessage(state.get(0).map(Authorizer.fromState), List(violation)),
          accountState =>
            state.updated(0, accountState) -> OutputMessage(acc.some, Nil)
      )
      case (state, trx: TransactionInfo) =>
        TransactionRules.checkAll(state.get(0))(trx).fold(
          violation  =>
            state -> OutputMessage(state.get(0).map(Authorizer.fromState), List(violation)),
          accState   =>
            state.updated(0, accState) -> OutputMessage(AccountInfo(accState.active, accState.amount).some, Nil)
        )
    }.map(_._2)

  def process(count: Int, limit: FiniteDuration): F[Unit] =
      authorize
      .groupWithin(count, limit)
      .evalMap(output => sink.batch(output.map(_.asJson.deepDropNullValues.noSpaces)))
      .compile
      .drain
}

object Authorizer {
  def make[F[_]: Sync: Temporal](consumer: StreamConsumer[F, InputMessage], sink: Sink[F]): F[Authorizer[F]] =
    Sync[F].pure(new Authorizer[F](consumer, sink))

  def fromState(state: AccountState): AccountInfo = AccountInfo(state.active, state.amount)
}
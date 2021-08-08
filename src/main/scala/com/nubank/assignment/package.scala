package com.nubank

import java.time.Instant

import cats.effect.Sync
import com.nubank.assignment.InputMessage.{AccountInfo, TransactionInfo}
import com.nubank.assignment.enums.Violation
import fs2.Pipe
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{ACursor, Decoder, DecodingFailure, Encoder}

import scala.concurrent.duration.FiniteDuration
import scala.util.Try

package object assignment {

  def logger[F[_]: Sync]: Pipe[F, String, String] = _.evalTap(
    m => Sync[F].delay(println(s"$m"))
  )
  final case class AccountState(active: Boolean, amount: Long, slidingWindow: List[TransactionInfo])
  final case class OutputMessage(account: Option[AccountInfo] = None, violations: List[Violation] = Nil)

  sealed trait InputMessage extends Product with Serializable
  object InputMessage {

    final case class AccountInfo(
      activeCard: Boolean,
      availableLimit: Long
    ) extends InputMessage

    final case class TransactionInfo(
      merchant: String,
      amount: Long,
      time: Instant
    ) extends InputMessage
  }

  object AccountState {
    def getWindow(state: AccountState)(trx: TransactionInfo, window: FiniteDuration): List[TransactionInfo] = {
      val windowTime = trx.time.toEpochMilli - window.toMillis
      state.slidingWindow.takeWhile(_.time.toEpochMilli >= windowTime)
    }

    def fromAccountInfo(accountInfo: AccountInfo): AccountState =
      AccountState(accountInfo.activeCard, accountInfo.availableLimit, Nil)
  }

  implicit val accountInfoDecoder: Decoder[AccountInfo]         = deriveDecoder
  implicit val transactionInfoDecoder: Decoder[TransactionInfo] = deriveDecoder
  implicit val accountInfoEncoder: Encoder[AccountInfo]         = deriveEncoder
  implicit val transactionInfoEncoder: Encoder[TransactionInfo] = deriveEncoder
  implicit val instantDecoder: Decoder[Instant]                 = Decoder.decodeString.emapTry { str => Try(Instant.parse(str)) }
  implicit val outputMessageEncoder: Encoder[OutputMessage]     = deriveEncoder

  implicit val inputMessageDecoder: Decoder[InputMessage] = Decoder.instance[InputMessage] {
    val supportedEvents = Set("account", "transaction")

    def decodeEvent(key: String, cur: ACursor): Decoder.Result[InputMessage] = key match {
      case "account"     => cur.as[AccountInfo]
      case "transaction" => cur.as[TransactionInfo]
      case _             => Left(DecodingFailure(s"Event type $key is not supported", cur.history))
    }

    cursor =>
      val error = DecodingFailure(s"Event type is not supported in ${cursor.keys}", cursor.history)
      for {
        key    <- cursor.keys.flatMap(_.find(supportedEvents contains _)).toRight(error)
        result <- decodeEvent(key, cursor.downField(key))
      } yield result
  }
}

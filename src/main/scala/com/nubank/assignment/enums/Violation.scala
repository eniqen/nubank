package com.nubank.assignment.enums

import enumeratum.{CirceEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.Lowercase

sealed abstract class Violation(override val entryName: String) extends EnumEntry with Lowercase

object Violation extends CirceEnum[Violation] with Enum[Violation] {
  val values: IndexedSeq[Violation] = findValues

  case object AccountAlreadyExist   extends Violation("account-already-initialized")
  case object AccountNotInitialised extends Violation("account-not-initialized")
  case object CardNotActivated      extends Violation("card-not-active")
  case object InsufficientLimit     extends Violation("insufficient-limit")
  case object HighFrequency         extends Violation("high-frequency-small-interval")
  case object DoubledTransaction    extends Violation("doubled-transaction")
}


package com.nubank.assignment.validation

import com.nubank.assignment.{AccountState}
import com.nubank.assignment.InputMessage.TransactionInfo
import com.nubank.assignment.enums.Violation

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object TransactionRules {
  def checkAccountExist(state: Option[AccountState])(trx: TransactionInfo): Either[Violation, AccountState] =
    Either.cond(state.isDefined, state.get, Violation.AccountNotInitialised)

  def checkCardActivated(state: AccountState)(trx: TransactionInfo): Either[Violation, AccountState] =
    Either.cond(state.active, state, Violation.CardNotActivated)

  def checkLimit(state: AccountState)(trx: TransactionInfo): Either[Violation, AccountState] =
    Either.cond((state.amount - trx.amount) >= 0, state.copy(amount = state.amount - trx.amount), Violation.InsufficientLimit)

  def checkFrequency(state: AccountState,
                     maxTransactionInWindow: Int,
                     timeWindow: FiniteDuration
  )(trx: TransactionInfo): Either[Violation, AccountState] = {
    val shrinkWindow = AccountState.getWindow(state)(trx, timeWindow)

    Either.cond(
      shrinkWindow.size < maxTransactionInWindow,
      state.copy(slidingWindow = trx :: shrinkWindow),
      Violation.HighFrequency
    )
  }

  def checkDoubleTransaction(state: AccountState)(trx: TransactionInfo): Either[Violation, AccountState] = {
    Either.cond(
      state.slidingWindow.groupBy(t => t.merchant -> t.amount).collectFirst { case (k, v) if v.size > 1 => k -> v}.isEmpty,
      state,
      Violation.DoubledTransaction
    )
  }

  def checkAll(s: Option[AccountState],
               timeWindow: FiniteDuration = 2.minutes,
               maxTrxInWindow: Int = 3
  )(trx: TransactionInfo): Either[Violation, AccountState] = {
    val All = List(
      (acc: AccountState) => checkCardActivated(acc)(trx),
      (acc: AccountState) => checkLimit(acc)(trx),
      (acc: AccountState) => checkFrequency(acc, maxTrxInWindow, timeWindow)(trx),
      (acc: AccountState) => checkDoubleTransaction(acc)(trx)
    )
    All.foldLeft(checkAccountExist(s)(trx))(_ flatMap _)
  }
}

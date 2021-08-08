package com.nubank.assignment.validation

import com.nubank.assignment.AccountState
import com.nubank.assignment.InputMessage.AccountInfo
import com.nubank.assignment.enums.Violation

object AccountRules {
  def checkInitialization(state: Option[AccountState])(acc: AccountInfo): Either[Violation, AccountState] =
    Either.cond(state.isEmpty, AccountState(acc.activeCard, acc.availableLimit, Nil), Violation.AccountAlreadyExist)

  def checkAll(state: Option[AccountState])(acc: AccountInfo): Either[Violation, AccountState] =
    checkInitialization(state)(acc)
}
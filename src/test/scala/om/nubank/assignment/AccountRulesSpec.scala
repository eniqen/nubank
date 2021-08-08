package om.nubank.assignment

import com.nubank.assignment.AccountState
import com.nubank.assignment.InputMessage.AccountInfo
import com.nubank.assignment.enums.Violation
import com.nubank.assignment.validation.AccountRules
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AccountRulesSpec extends AnyFlatSpec with Matchers {

  it should "check when account first time initialized" in {
    val state: Option[AccountState] = None
    val acc                         = AccountInfo(true, 100)
    val result = AccountRules.checkInitialization(state)(acc)

    result shouldBe Right(AccountState(acc.activeCard, acc.availableLimit, Nil))
  }

  it should "check when account has already been initialized" in {
    val acc = AccountInfo(true, 100)
    val state: Option[AccountState] = Some(AccountState(acc.activeCard, acc.availableLimit, Nil))
    val result = AccountRules.checkInitialization(state)(acc)

    result shouldBe Left(Violation.AccountAlreadyExist)
  }

}

package om.nubank.assignment

import java.time.Instant

import com.nubank.assignment.AccountState
import com.nubank.assignment.InputMessage.TransactionInfo
import com.nubank.assignment.enums.Violation
import com.nubank.assignment.validation.TransactionRules
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.DurationInt

class TransactionRulesSpec extends AnyFlatSpec with Matchers {

  it should "check when account is not initialised" in {
    //given
    val account: Option[AccountState] = None
    val transaction                   = TransactionInfo("noname", 0, Instant.now())
    //when
    val result                        = TransactionRules.checkAccountExist(account)(transaction)
    //then
    result shouldBe Left(Violation.AccountNotInitialised)
  }

  it should "check when account is initialised" in {
    //given
    val account                       = AccountState(active = true, 0, Nil)
    val transaction                   = TransactionInfo("noname", 0, Instant.now())
    //when
    val result                        = TransactionRules.checkAccountExist(Some(account))(transaction)
    //then
    result shouldBe Right(account)
  }

  it should "check when card is activated" in {
    //given
    val account                       = AccountState(active = true, 0, Nil)
    val transaction                   = TransactionInfo("noname", 0, Instant.now())
    //when
    val result                        = TransactionRules.checkCardActivated(account)(transaction)
    //then
    result shouldBe Right(account)
  }

  it should "check when card is not activated" in {
    //given
    val account                       = AccountState(active = false, 0, Nil)
    val transaction                   = TransactionInfo("noname", 0, Instant.now())
    //when
    val result                        = TransactionRules.checkCardActivated(account)(transaction)
    //then
    result shouldBe Left(Violation.CardNotActivated)
  }

  it should "check limit when amount is greater" in {
    //given
    val account                       = AccountState(active = true, 100, Nil)
    val transaction                   = TransactionInfo("noname", 10, Instant.now())
    //when
    val result                        = TransactionRules.checkLimit(account)(transaction)
    //then
    result shouldBe Right(account.copy(amount = account.amount - transaction.amount))
  }

  it should "check limit when amount is less" in {
    //given
    val account                       = AccountState(active = true, 10, Nil)
    val transaction                   = TransactionInfo("noname", 11, Instant.now())
    //when
    val result                        = TransactionRules.checkLimit(account)(transaction)
    //then
    result shouldBe Left(Violation.InsufficientLimit)
  }

  it should "check count of transactions > than expected in time window" in {
    //given
    val time                          = Instant.now
    val transactions                  = List(
      TransactionInfo("1", 10, time.minusSeconds(1)),
      TransactionInfo("2", 10, time.minusSeconds(2)),
      TransactionInfo("3", 10, time.minusSeconds(3))
    )
    val account                       = AccountState(active = true, 10, transactions)
    val transaction                   = TransactionInfo("noname", 11, time)
    //when
    val result                        = TransactionRules.checkFrequency(account, 3, 3.seconds)(transaction)
    //then
    result shouldBe Left(Violation.HighFrequency)
  }

  it should "check count of transactions <= than expected in time window" in {
    //given
    val time                          = Instant.now
    val transactions                  = List(
      TransactionInfo("1", 10, time.minusSeconds(1)),
      TransactionInfo("2", 10, time.minusSeconds(2))
    )
    val account                       = AccountState(active = true, 10, transactions)
    val transaction                   = TransactionInfo("noname", 11, time)
    //when
    val result                        = TransactionRules.checkFrequency(account, 3, 3.seconds)(transaction)
    //then
    result shouldBe Right(account.copy(slidingWindow = transaction :: account.slidingWindow))
  }

  it should "check duplicates are not existed in time window" in {
    //given
    val time                          = Instant.now
    val transactions                  = List(
      TransactionInfo("1", 10, time.minusSeconds(1)),
      TransactionInfo("2", 10, time.minusSeconds(2))
    )
    val account                       = AccountState(active = true, 10, transactions)
    val transaction                   = TransactionInfo("noname", 11, time)
    //when
    val result                        = TransactionRules.checkDoubleTransaction(account)(transaction)
    //then
    result shouldBe Right(account)
  }

  it should "check duplicates are existed in time window" in {
    //given
    val time                          = Instant.now
    val transactions                  = List(
      TransactionInfo("1", 10, time.minusSeconds(1)),
      TransactionInfo("1", 10, time.minusSeconds(2))
    )
    val account                       = AccountState(active = true, 10, transactions)
    val transaction                   = TransactionInfo("noname", 11, time)
    //when
    val result                        = TransactionRules.checkDoubleTransaction(account)(transaction)
    //then
    result shouldBe Left(Violation.DoubledTransaction)
  }

  it should "check All combinations together" in {
    //given
    val time                          = Instant.now
    val transactions                  = List(
      TransactionInfo("1", 10, time.minusSeconds(1)),
      TransactionInfo("2", 10, time.minusSeconds(2)),
      TransactionInfo("3", 10, time.minusSeconds(4)),
      TransactionInfo("2", 10, time.minusSeconds(6)),
      TransactionInfo("3", 10, time.minusSeconds(8)),
      TransactionInfo("1", 10, time.minusSeconds(10))
    )
    val account                       = AccountState(active = true, 100, transactions)
    val transaction                   = TransactionInfo("noname", 11, time)
    val timeWindow                    = 3.second
    //when
    val result                        = TransactionRules.checkAll(Some(account), timeWindow, 3)(transaction)
    //then
    result shouldBe Right(
      account.copy(
        amount = account.amount - transaction.amount,
        slidingWindow = transaction :: account.slidingWindow.filter(
          transaction.time.toEpochMilli - _.time.toEpochMilli <= timeWindow.toMillis
        )
      )
    )
  }
}

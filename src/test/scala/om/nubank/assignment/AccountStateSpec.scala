package om.nubank.assignment

import java.time.Instant

import com.nubank.assignment.AccountState
import com.nubank.assignment.InputMessage.TransactionInfo
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.DurationInt

class AccountStateSpec extends AnyFlatSpec with Matchers {

  val transactions = List(
    TransactionInfo("McDonald's", 20, Instant.parse("2019-02-13T11:01:31.000Z")),
    TransactionInfo("Habbib's", 20, Instant.parse("2019-02-13T11:00:31.000Z")),
    TransactionInfo("Burger King", 20, Instant.parse("2019-02-13T11:00:00.000Z"))
  )

  it should "return only 2 minutes window" in {
    //given
    val trx        = TransactionInfo("Subway", 20, Instant.parse("2019-02-13T11:02:00.000Z"))
    val account    = AccountState(active = true, 1000, transactions)
    val timeWindow = 2.minutes

    //when
    val window = AccountState.getWindow(account)(trx, timeWindow)

    //then
    window should contain theSameElementsAs account.slidingWindow
    window.forall( t =>
      (trx.time.toEpochMilli - t.time.toEpochMilli) >= timeWindow.toMillis
    )
  }

  it should "return nothing in 1 second window" in {
    //given
    val trx         = TransactionInfo("Burger King", 10, Instant.parse("2019-02-13T11:01:32.000Z"))
    val account     = AccountState(active = true, 1000, transactions)
    val timeWindow  = 1.second

    //when
    val window = AccountState.getWindow(account)(trx, timeWindow)

    //then
    window should have size 1
    (transactions.head.time.toEpochMilli - window.head.time.toEpochMilli) <= timeWindow.toMillis

    transactions.tail.forall(
      didNotMatchTrx => trx.time.getEpochSecond - didNotMatchTrx.time.getEpochSecond > timeWindow.toMillis
    )
  }
}
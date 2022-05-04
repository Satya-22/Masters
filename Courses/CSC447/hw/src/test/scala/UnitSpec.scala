import org.scalatest.concurrent.{Signaler, TimeLimitedTests}
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import org.scalatest.time.{Second, Span}

abstract class UnitSpec
  extends AnyPropSpec
    with Matchers
    with TimeLimitedTests
{
  val timeLimit = Span(1, Second)
  override val defaultTestSignaler = new Signaler {
    @deprecated("","")
    override def apply (testThread: Thread) : Unit = {
      println ("Too slow")
      testThread.stop () // deprecated. unsafe. do not use
    }
  }
}

import org.scalatest.*
import org.scalatest.prop.TableDrivenPropertyChecks.*

class subtypingtests extends UnitSpec:
  val EX : Map[Int, Tag] = 
    (for i <- (1 to 3).toList yield {
      object T extends Tag ("subtypingex%02d".format (i))
      (i, T)
    }).toMap

  import subtyping.*

  property ("EX01 - observeCounter", EX (1)) {
    val rand = scala.util.Random
    var total = rand.nextInt (100)
    val res = observeCounter {
      case c:Counter =>
        for i <- 1 to total do
          if rand.nextBoolean() then
            c.increment()
          else
            c.decrement()
    }
    assert (res === total)
  }

  property ("EX02 - observeCounterList", EX (2)) {
    val rand = scala.util.Random
    var totals : List[Int] = (1 to 3).toList.map (_ => rand.nextInt (100))
    val res : List[Int] = observeCounterList {
      case cs:List[Counter] =>
        if cs.length != 3 then 
          throw RuntimeException ("Length of list must be exactly 3")
        for (total, c) <- totals.zip (cs) do 
          for i <- 1 to total do 
            if rand.nextBoolean() then
              c.increment()
            else
              c.decrement()
    }
    assert (res === totals)
  }

  property ("EX03 - observeCounterArray", EX (3)) {
    val rand = scala.util.Random
    var totals : List[Int] = (1 to 3).toList.map (_ => rand.nextInt (100))
    val res : Array[Int] = observeCounterArray {
      case cs:Array[Counter] =>
        if cs.length != 3 then
          throw RuntimeException ("Length of list must be exactly 3")
        for (total, c) <- totals.zip (cs) do 
          for i <- 1 to total do 
            if rand.nextBoolean() then 
              c.increment()
            else
              c.decrement()
    }
    assert (res.toList === totals)
  }


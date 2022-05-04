import org.scalatest.*
import org.scalatest.prop.TableDrivenPropertyChecks.*

class interpretertests extends UnitSpec:
  val EX : Map[Int, Tag] = 
    (for i <- (1 to 12).toList yield {
      object T extends Tag ("interpreterex%02d".format (i))
      (i, T)
    }).toMap

  import interpreter.*
  import Val.*
  import Exp.*

  property ("EX01 - exFiftyVal", EX (1)) {
    assert (exFiftyVal.asInstanceOf[VInt].n === 50)
  }

  property ("EX02 - exFiftyExp", EX (2)) { 
    assert (exFiftyExp.asInstanceOf[EVal].v.asInstanceOf[VInt].n === 50)
  }

  property ("EX03 - exX", EX (3)) { 
    assert ((exX ("x")).asInstanceOf[VInt].n === 50)
    assert (exX.keySet === List ("x").toSet)
  }

  property ("EX04 - exXPlusFifty", EX (4)) { 
    assert (exXPlusFifty.asInstanceOf[EApp].op.asInstanceOf[EApp].arg.asInstanceOf[EVar].nm === "x")
    assert (exXPlusFifty.asInstanceOf[EApp].arg.asInstanceOf[EVal].v.asInstanceOf[VInt].n === 50)
    assert (evaluate (exX, exXPlusFifty) === VInt (100))
    assert (evaluate (Map ("x" -> VInt (10)), exXPlusFifty) === VInt (60))
    assert (prettyPrintExp (exXPlusFifty) === "(([fun: _+_] x) 50)")
  }

  property ("EX05 - exXFunPlusFifty", EX (5)) { 
    assert (exXFunPlusFifty.asInstanceOf[EAbs].argNm === "x")
    assert (evaluate (exX, exXFunPlusFifty).asInstanceOf[VClosure].env === exX)
    assert (prettyPrintExp (exXFunPlusFifty) === "(x => (([fun: _+_] x) 50))")
  }

  property ("EX06 - exXFunPlusFiftyApplied", EX (6)) { 
    assert (exXFunPlusFiftyApplied.asInstanceOf[EApp].arg.asInstanceOf[EVal].v.asInstanceOf[VInt].n === 30)
    assert (evaluate (exX, exXFunPlusFiftyApplied).asInstanceOf[VInt].n === 80)
    assert (prettyPrintExp (exXFunPlusFiftyApplied) === "((x => (([fun: _+_] x) 50)) 30)")
  }

  property ("EX07 - exLetXLetY", EX (7)) { 
    assert (exLetXLetY.asInstanceOf[ELet].nm === "x")
    assert (exLetXLetY.asInstanceOf[ELet].e.asInstanceOf[EVal].v.asInstanceOf[VInt].n === 5)
    assert (exLetXLetY.asInstanceOf[ELet].body.asInstanceOf[ELet].nm === "y")
    assert (exLetXLetY.asInstanceOf[ELet].body.asInstanceOf[ELet].e.asInstanceOf[EVal].v.asInstanceOf[VInt].n === 6)
    assert (evaluate (emptyEnv, exLetXLetY).asInstanceOf[VInt].n === 11)
    assert (prettyPrintExp (exLetXLetY) === "(let x = 5 in (let y = 6 in (([fun: _+_] x) y)))")
  }

  property ("EX08 - exLetF", EX (8)) { 
    assert (exLetF.asInstanceOf[ELet].nm == "f")
    assert (exLetF.asInstanceOf[ELet].e.asInstanceOf[EAbs].argNm == "x")
    assert (exLetF.asInstanceOf[ELet].body.asInstanceOf[EApp].op.asInstanceOf[EVar].nm == "f")
    assert (exLetF.asInstanceOf[ELet].body.asInstanceOf[EApp].arg.asInstanceOf[EVal].v.asInstanceOf[VInt].n == 5)
    assert (evaluate (emptyEnv, exLetF).asInstanceOf[VInt].n == 55)
    assert (prettyPrintExp (exLetF) === "(let f = (x => (([fun: _+_] x) 50)) in (f 5))")
  }

  // EX09: Cannot test this one, because it requires EIf to be defined in order to compile.

  property ("EX10 - exPair", EX (10)) { 
    assert (exPair.asInstanceOf[EPair].fst.asInstanceOf[EVal].v.asInstanceOf[VInt].n === 5)
    assert (exPair.asInstanceOf[EPair].snd.asInstanceOf[EAbs].argNm === "x")
    assert (exPair.asInstanceOf[EPair].snd.asInstanceOf[EAbs].body.asInstanceOf[EVal].v.asInstanceOf[VInt].n === 6)
    assert (evaluate (emptyEnv, exPair).asInstanceOf[VPair].fst.asInstanceOf[VInt].n === 5)
    assert (evaluate (emptyEnv, exPair).asInstanceOf[VPair].snd.asInstanceOf[VClosure].env === emptyEnv)
    assert (evaluate (emptyEnv, exPair).asInstanceOf[VPair].snd.asInstanceOf[VClosure].argNm === "x")
    assert (evaluate (emptyEnv, exPair).asInstanceOf[VPair].snd.asInstanceOf[VClosure].body.asInstanceOf[EVal].v.asInstanceOf[VInt].n === 6)
    assert (prettyPrintExp (exPair) === "(5, (x => 6))")
  }

  property ("EX11 - exCounter", EX (11)) { 
    assert (exCounter.asInstanceOf[EFst].e.asInstanceOf[EApp].arg.asInstanceOf[EVal].v == VNil)
    assert (prettyPrintExp (exCounter) === "(fst ((snd (((counterFunction n => (m => (n, (counterFunction (([fun: _+_] n) 1))))) 4) Nil)) Nil))")
  }

  // EX12: Cannot test this one, because it requires a written answer.


// The src directory is meant for code that will be run with automated tests.
// Put other code in this directory.
//
// sbt requires that all files in src/main/scala must compile.
// That can be a pain when you want to experiment.
//
// In the scala console, you can load this file by typing
//    scala> :load play/README.scala
//    scala> length1(xs)
// 
val xs = List(11, 21, 31)
val xss = List(List(11, 21), List(), List(31), List(41, 51, 61))

def log[X](prefix: String, d:Int=0)(computeResult: => X) =
  val indent = "  " * d
  println(s"${indent}${prefix}")
  val result = computeResult
  println(s"${indent}${prefix} : ${result}")
  result

// This is the length function from the first lecture on scala, with logging
// This version is right recursive, computing the operator with the RESULT of
// recursive call.
def length1[X](xs:List[X]): Int =
  log(s"length($xs)") {
    xs match
      case Nil     => 0
      case _ :: ys => 1 + length1(ys)
  }

// Here is a version that recurs into the parameter instead of the result. This
// version is left recursive, AKA tail recursive, computing the operator into a
// PARAMETER of the recursive call.
//
// This version also uses the optional parameter d to indicate the call depth.
def length2[X](xs:List[X], accumulator:Int = 0, d:Int = 0): Int = 
  log(s"length($xs, $accumulator)", d) {
    xs match
      case Nil     => accumulator
      case _ :: ys => length2(ys, 1 + accumulator, d + 1)
  }

// Try this:
//    scala> :load play/README.scala  
//    scala> length1(xs)
//    scala> length2(xs)
// Try changing length1 to use a depth parameter for indentation.
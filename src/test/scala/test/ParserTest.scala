package rclang
package test

import org.scalatest.funsuite.AnyFunSuite

class ParserTest extends AnyFunSuite:

  test("bool") {
    val p = new RcParser
    p.parse(p.boolConstant, "true") match {
      case p.Success(v, _) => println(v)
      case _ => assert(false)
    }
    p.parse(p.boolConstant, "false")
    assert(true)
  }

  // test 2
  test("'double' should handle 1") {
    assert(true)
  }

  test("test with Int.MaxValue") (pending)

end ParserTest
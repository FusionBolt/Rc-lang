package rclang
package codegen

import mir.*

import rclang.ty.NilType

class IRTranslatorTest extends RcTestBase {
  describe("binary") {
    it("ok") {
      val tt = IRTranslator()
      val bn = Binary("Add", Integer(1), Integer(2));
      val bb = BasicBlock("0", List(bn))
      tt.visitBB(bb)
    }
  }

  def addFun = {
    val bn = Binary("Add", Integer(1), Integer(2));
    val entry = BasicBlock("0", List(bn))
    val f = Function("f", NilType, List(), entry, List(entry))
    f
  }

  describe("call") {
    it("ok") {
      val f = addFun
      val tt = IRTranslator()
      val call = Call(f, List());
      val bb = BasicBlock("0", List(call))
      tt.visitBB(bb)
    }
  }

  describe("branch") {
    it("ok") {
      val exitBB = BasicBlock("b3", List())
      val elseBB = BasicBlock("b2", List(
        Branch(exitBB)
      ))
      val thenBB = BasicBlock("b1", List(
        Branch(exitBB)
      ))
      val entry = BasicBlock("b0", List(
        CondBranch(true, thenBB, elseBB)
      ))
      val tt = IRTranslator()
      tt.visitBB(entry)
    }
  }

  describe("function") {
    it("ok") {
      val f = addFun
      val t = IRTranslator()
      t.visit(f)
    }
  }
}

package rclang
package ty
import org.scalatest.funspec.AnyFunSpec
import ast.Expr.*
import ast.Stmt.*
import ast.*
import ast.ImplicitConversions.*
import ast.{ASTBuilder, RcModule}

import ty.Int32Type

class TypedTranslatorTest extends AnyFunSpec with ASTBuilder {

  def getFirstMethodFromModule(module: RcModule): Method = {
    module.items.head match
      case m: Method => m
      case _ => ???
  }
  describe("AddLocal") {
    it("succeed") {
      val m = RcModule(List(makeASTMethod("f", block = List(
        Local("a", TyInfo.Infer, Number(1)),
        Stmt.Expr(Identifier("a"))
      ))))
      val result = TypedTranslator(TyCtxt())(m)
      val ty = getFirstMethodFromModule(m).body.stmts.last.asInstanceOf[Stmt.Expr].expr.asInstanceOf[Identifier].ty
      assert(ty == Int32Type)
    }
  }

  describe("if") {
    it("succeed") {
      val m = mkFnInMod("f", block = List(
        Stmt.Expr(makeIf(
          Bool(true),
          makeExprBlock(Number(1)),
          makeExprBlock(Number(2)),
        ))
      ))
      val result = TypedTranslator(TyCtxt())(m)
      val ty = getFirstMethodFromModule(m).body.stmts.head.asInstanceOf[Stmt.Expr].expr.asInstanceOf[If].ty
      assert(ty == Int32Type)
    }
  }
}

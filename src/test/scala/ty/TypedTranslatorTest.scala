package rclang
package ty
import org.scalatest.funspec.AnyFunSpec
import ast.Expr.*
import ast.Stmt.*
import ast.*
import ast.ImplicitConversions.*
import ast.{ASTBuilder, RcModule}

import ty.Type.Int32

class TypedTranslatorTest extends AnyFunSpec with ASTBuilder {
  val tyCtxt = TyCtxt()

  describe("AddLocal") {
    it("succeed") {
      val m = RcModule(List(makeASTMethod("f", block = List(
        Local("a", TyInfo.Infer, Number(1)),
        Stmt.Expr(Identifier("a"))
      ))))
      val result = TypedTranslator(tyCtxt)(m)
      val ty = result.items.head.asInstanceOf[Item.Method].body.stmts.last.asInstanceOf[Stmt.Expr].expr.asInstanceOf[Identifier].ty
      assert(ty == Int32)
    }
  }
}

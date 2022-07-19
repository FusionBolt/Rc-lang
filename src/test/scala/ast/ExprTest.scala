package rclang
package ast

import ast.ImplicitConversions.*
import ast.Expr.*

import rclang.mir.BasicBlock

class ExprTest extends RcTestBase with ASTBuilder {
  describe("noCapturedLambdaToMethod") {
    // todo: uuid 不合适
    it("ok") {
      val a = BasicBlock("a")
      val b = BasicBlock("a")
      println(a eq b)
      println(a == b)
      val lambda = Lambda(
        Params(List(Param("a", TyInfo.Spec("Int")))),
        Block(List(Stmt.Expr(Expr.Binary(BinaryOp.Add, Expr.Identifier("a"), Expr.Number(1))))))
      val m = lambdaToMethod(lambda.asInstanceOf[Expr.Lambda])
    }
  }
}

package rclang
package analysis

import ast.Item.*
import ast.{ASTBuilder, Expr, RcModule}
import ast.Expr.*
import ast.ImplicitConversions.*
import analysis.SymScanner

import org.scalatest.funspec.AnyFunSpec
import rclang.tools.LocalEntry

class SymScannerTest extends AnyFunSpec with ASTBuilder {
  describe("Class") {
    it("default Kernel") {
      assert(SymScanner(RcModule(List())).classes == Set(Def.Kernel))
    }
    it("ok") {
      val global = SymScanner(RcModule(List(mkASTClass("F1"), mkASTClass("F2"))))
      assert(global.classes == Set(Def.Kernel, "F1", "F2"))
    }
  }

  describe("KernelMethod") {
    it("ok") {
      val global = SymScanner(RcModule(List(makeASTMethod("f1"), makeASTMethod("f2"))))
      assert(global.classTable(Def.Kernel).methods.keys == Set("f1", "f2"))
    }
  }

  describe("LocalTable") {
    val localB = makeLocal("b", Number(3))
    val localA = makeLocal("a", Number(1))
    val f1 = makeASTMethod("f1",
      block = List(localB, localA))
    val localE = makeLocal("e", Number(7))
    val localF = makeLocal("f", Number(3))
    val f2 = makeASTMethod("f2",
      block = List(localE, localF))

    it("SingleMethod") {
      val module = RcModule(List(f1))
      val t = SymScanner(module).kernel.methods("f1")
      assert(t.locals("b") == LocalEntry(0, localB))
      assert(t.locals("a") == LocalEntry(1, localA))
    }

    it("MultiMethod") {
      val module = RcModule(List(f2, f1))
      val t = SymScanner(module).kernel.methods
      val f1T = t("f1")
      assert(f1T.locals("b") == LocalEntry(0, localB))
      assert(f1T.locals("a") == LocalEntry(1, localA))
      val f2T = t("f2")
      assert(f2T.locals("e") == LocalEntry(0, localE))
      assert(f2T.locals("f") == LocalEntry(1, localF))
    }
  }
}

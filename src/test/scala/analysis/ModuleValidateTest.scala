package rclang
package analysis

import org.scalatest.funspec.AnyFunSpec
import ast.ImplicitConversions.*
import ast.*
import analysis.ModuleValidate

class ModuleValidateTest extends AnyFunSpec with ASTBuilder with ModuleValidate {
  describe("ASTValidateTest") {
    describe("fieldDefValid") {
      describe("SingleDef") {
        it("true") {
          val f = FieldDef("a", TyInfo.Infer, None)
          assert(fieldDefValid(f) == List(ValidateError(f, "Field without initValue need spec Type")))
        }

        it("false") {
          val f1 = FieldDef("a", TyInfo.Spec("Int"), None)
          assert(fieldDefValid(f1) == List())
          val f2 = FieldDef("a", TyInfo.Infer, Some(Expr.Number(1)))
          assert(fieldDefValid(f2) == List())
        }
      }
    }

    it("should methodDeclValid") {

    }

    // todo: test multi error method
    describe("methodsDeclValid") {
      it("true") {
        val fs = List(
          makeASTMethod("f1"),
          makeASTMethod("f2"),
          makeASTMethod("f1")
        ).map(_.decl)
        assert(methodsDeclValid(fs) == List(ValidateError(fs(2).name, "Method Ident(f1) Dup")))
      }

      it("false") {
        val fs = List(
          makeASTMethod("f1"),
          makeASTMethod("f2"),
          makeASTMethod("f3")
        ).map(_.decl)
        assert(methodsDeclValid(fs) == List())
      }
    }

    it("should methodsValid") {

    }

    describe("dupCheck") {
      it("true") {
        val a = List("a", "b", "c", "c").map(Ident)
        assert(dupCheck(a, "Name") == List(ValidateError(a(3), s"Name Ident(c) Dup")))
      }

      it("false") {
        val a = List("a", "b", "c", "d").map(Ident)
        assert(dupCheck(a, "Name") == List())
      }
    }
  }
}

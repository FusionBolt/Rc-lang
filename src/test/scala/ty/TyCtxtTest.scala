package rclang
package ty
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.BeforeAndAfter
import ast.ImplicitConversions.*
import ty.*
import ast.Ident

class TyCtxtTest extends AnyFunSpec with BeforeAndAfter {
  var tyCtxt: TyCtxt = _
  before {
    tyCtxt = TyCtxt(Map(Ident("a") -> Int32Type, Ident("b") -> FloatType))
  }

  describe("enter") {
    it("returnCallResult") {
      val ty = Nil
      val t = tyCtxt.enter((() => {
        ty
      })())
      assert(t == ty)
    }

    it("block") {
      val ty = StringType
      val t = tyCtxt.enter({
        ty
      })
      assert(t == ty)
    }
  }

  describe("addLocal") {
    it("succeed") {
      tyCtxt.enter(() => {
        val id = Ident("n")
        val ty = NilType
        tyCtxt.addLocal(id, ty)
        assert(tyCtxt.lookup(id).contains(ty))
      })
      assert(tyCtxt.lookup(Ident("n")).isEmpty)
    }

    it("sameWithGlobal") {
      tyCtxt.enter(() => {
        val id = Ident("a")
        val ty = NilType
        tyCtxt.addLocal(id, ty)
        assert(tyCtxt.lookup(id).contains(ty))
      })
    }

    it("nested") {
      tyCtxt.enter(() => {
        val id = Ident("a")
        val ty = NilType
        tyCtxt.addLocal(id, ty)
        tyCtxt.enter(testEnter(id))
        assert(tyCtxt.enter(id) == StringType)
        assert(tyCtxt.lookup(id).contains(ty))
      })
    }

    def testEnter(id: Ident): Type = {
      assert(tyCtxt.local.isEmpty)
      val innerTy = StringType
      tyCtxt.addLocal(id, innerTy)
      assert(tyCtxt.lookup(id).contains(innerTy))
      innerTy
    }
  }
}

package rclang
package tools

import org.scalatest.funspec.AnyFunSpec

class StateTest extends AnyFunSpec {

  describe("StateTest") {
    it("NormalBy") {
      val s = State("Str")
      val result = s.by("tmp") { () =>
        assert(s.value == "tmp")
      }
      assert(result == "tmp")
      assert(s.value == "Str")
    }

    // modify
    it("ModifyInBody") {
      val s = State("Str")
      val result = s.by("tmp") { () =>
        s.value = "what"
      }
      assert(result == "what")
      assert(s.value == "Str")
    }
  }
}

package rclang
package codegen

import compiler.Driver
import compiler.Driver.{codegen, genASM, parse, typeProc}
import mir.*
import tools.RcLogger.{log, logf}

class PrintTest extends RcTestBase {
  def getModule(src: String) = {
    val ast = parse(src)
    val (typedModule, table) = typeProc(ast)
    val mirMod = log(ToMIR(table).proc(typedModule), "ToMIR")
    logf("mir.txt", mirMod)
    mirMod
  }

  def codegen(mirMod: Module) = {
    val fns = log(toLIR(mirMod), "ToLIR")
    logf("LIR.txt", fns.mkString("\n\n"))
    genASM(fns)
  }

  val src = """def main()
              |    print("Hello world")
              |end""".stripMargin

  val mod = getModule(src)

  describe("success") {
    it("ok") {
      val asm = codegen(mod)
      val expect = """.section .text
                      |  .globl add
                      |  .type  add, @function
                      |add:
                      |  pushq %rbp
                      |  movq %rsp, %rbp
                      |  movl %edi, %ecx
                      |  movl %esi, %esi
                      |  addl %ecx, %esi
                      |  movl %esi, %eax
                      |  popq %rbp
                      |  ret
                      |.LFE0:
                      |  .ident "RCC: 0.0.1"
                      |""".stripMargin
      assert(asm == expect)
    }
  }
}

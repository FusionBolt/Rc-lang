package rclang
package codegen

import compiler.Driver
import compiler.Driver.{codegen, genASM, parse, typeProc}
import mir.*
import tools.RcLogger.{log, logf}

class SimpleTest extends RcTestBase {
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

  def getASM(src: String) = {
    codegen(getModule(src))
  }

  ignore("print") {
    val src = """def main()
                |    print("Hello world")
                |end
                |""".stripMargin

    it("ok") {
      val asm = getASM(src).strip
      val expect = """.section .text
                     |  .globl main
                     |  .type  main, @function
                     |main:
                     |  pushq %rbp
                     |  movq %rsp, %rbp
                     |  leaq .LC0(%rip), %rax
                     |  movq %rax, %rdi
                     |  call puts@PLT
                     |  movl $0, %eax
                     |  popq %rbp
                     |  ret
                     |.LFE0:
                     |  .ident "RCC: 0.0.1"
                     |.section .rodata
                     |.LC0:
                     |  .string "Hello world"""".stripMargin
      asm should be (expect)
    }
  }

  ignore("add") {
    val src = """def add(a: Int, b: Int)
                |  a + b
                |end
                |""".stripMargin
    it("ok") {
      val asm = getASM(src)
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
      println(asm)
      asm should be(expect)
    }
  }
}

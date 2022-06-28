package rclang
package Interface

import scala.io.Source
import lexer.*
import parser.RcParser
import analysis.SymScanner

import rclang.mir.ToMIR
import rclang.ty.{Infer, TyCtxt, TypeCheck, TypedTranslator}

import java.io.{File, PrintWriter}

object Compile {
  // todo:should not in interface
  def apply(option: CompileOption): Unit = {
    val f = Source fromFile option.srcPath
    // avoid last line is end and lost last empty line
    val src = f.getLines.mkString("\n") + "\n"
    f.close()
    println(src)
    val tokens = Lexer(src) match {
      case Left(value) => throw RuntimeException(value.msg)
      case Right(value) => value
    }
    println("Lexer Finish")
    // todo:dump tokens
    dumpTokens(tokens)
    val ast = RcParser(tokens) match {
      case Left(value) => throw RuntimeException(value.msg)
      case Right(value) => value
    }
    println("Parser Finish")
    // todo:dump ast
    println(ast)
    val table = SymScanner(ast).methodTypeTable.toMap
    val tyCtxt = TyCtxt(table.map((id, item) => id -> Infer(item)))
    // todo:dump table and TyCtxt
    val typedModule = TypedTranslator(tyCtxt)(ast)
    // todo:dump typedModule
    TypeCheck(typedModule)

    val funList = ToMIR(table).proc(typedModule)
    println(funList(0).instructions.map(_.toString).mkString("\n"))
  }

  def dumpTokens(tokens: List[Token]) = {
    val str = tokens.mkString(" ")
    val f = new PrintWriter(new File("tokens.txt"));
    f.write(str)
    f.close()
  }
}

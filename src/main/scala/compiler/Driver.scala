package rclang
package compiler

import analysis.{BasicAA, SymScanner}
import lexer.Lexer
import mir.*
import parser.RcParser
import pass.AnalysisManager
import tools.*
import ast.ClassesRender
import ty.{Infer, TyCtxt, Type, TypeCheck, TypedTranslator}
import tools.RcLogger.{log, logf}
import analysis.Analysis.given
import ast.{Class, Ident, Item, RcModule}

import rclang.codegen.toLIR

import scala.io.Source

object Driver {
  def getSrc(path: String) = {
    val f = Source fromFile path
    // avoid last line is end and lost last empty line
    val src = f.getLines.mkString("\n") + "\n"
    f.close()
    src
  }

  def parse(src: String): RcModule = {
    val tokens = log(Lexer(src).unwrap, "Lexer")
    logf("token.txt", tokens.mkString(" ").replace("EOL", "\n"))
    log(RcParser(tokens).unwrap, "Parser").tap {
      logf("ast.txt", _)
    }
  }

  def typeProc(ast: RcModule): (RcModule, Map[Ident, Item]) = {
    val table = SymScanner(ast)
    val tyCtxt = TyCtxt()
    tyCtxt.setGlobalTable(table)
    val typedModule = TypedTranslator(tyCtxt)(ast)
    logf("typedModule.txt", typedModule)
    ClassesRender().rendClasses("classes.dot", "RcDump", typedModule.items collect { case i: Class => i })
    TypeCheck(typedModule)
    (typedModule, tyCtxt.globalTable.methodTypeTable.toMap)
  }

  // todo:should not in interface
  def apply(option: CompileOption): Unit = {
    DumpManager.mkDumpRootDir
    val src = getSrc(option.srcPath)
    val ast = parse(src)
    val (typedModule, table) = typeProc(ast)
    val mirMod = log(ToMIR(table).proc(typedModule), "ToMIR")
    codegen(mirMod)
  }

  def codegen(mirMod: Module) = {
    val machineIR = toLIR(mirMod)
//    println(machineIR.name)
    println(machineIR)
    logf("LIR.txt", machineIR)
  }
}

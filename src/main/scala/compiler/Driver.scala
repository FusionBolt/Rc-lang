package rclang
package compiler

import analysis.{BasicAA, DomTreeAnalysis, SymScanner}
import lexer.Lexer
import mir.*
import parser.RcParser
import pass.AnalysisManager
import tools.*
import ast.ClassesRender
import ty.{Infer, TyCtxt, Type, TypeCheck, TypedTranslator}
import tools.RcLogger.{log, logf}
import analysis.Analysis.given

import ast.{Ident, Item, RcModule, Class}

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
//    domTree(mirMod)
  }

//  def domTree(mirMod: Module) = {
//    val main = mirMod.fnTable.values.head
//    val begin = main.getBB("0")
//    val tBr = main.getBB("1")
//    val end = main.getBB("3")
//    logf("mir.txt", main)
//    CFGRender().rendFn("main.dot", "RcDump", main)
//    val tree = DomTreeBuilder().build(main)
//    println(tree.nodes.keys.map(_.name).mkString(","))
//
//    val n1 = tree.node(begin)
//    val n2 = tree.node(end)
//    val d = n1 dom n2
//
//    println(tree.nodes.contains(tBr))
//
//    val r = n1 dom n2
//    val id = sdom(n1, n2)
//    println("is Dom: " + d)
//    println("is IDom: " + id)
//    var am = AnalysisManager[Function]()
//    am.addAnalysis(DomTreeAnalysis())
//    am.addAnalysis(BasicAA())
//    var domTree = am.getResult[DomTreeAnalysis](main)
//    var aa = am.getResult[BasicAA](main)
//    logf("domTree.txt", tree.toString)
//  }
}

package rclang
package compiler

import analysis.{BasicAA, DomTreeAnalysis, LoopAnalysis, SymScanner}
import lexer.Lexer
import mir.*
import parser.RcParser
import pass.{AnalysisManager, PassManager, CFGSimplify}
import tools.*
import ast.ClassesRender
import ty.{Infer, TyCtxt, Type, TypeCheck, TypedTranslator}
import tools.RcLogger.{log, logf, warning}
import codegen.MachineFunction
//import analysis.Analysis.`given`
import analysis.Analysis.given_DomTreeAnalysis
import analysis.Analysis.given_LoopAnalysis
import ast.{Class, Ident, Item, RcModule}
import codegen.{GNUAssembler, GNUASM, RDataSection, StrSection, TextSection, toLIR}

import scala.sys.process.*
import scala.io.Source
import java.io.File
import java.nio.file.Path

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

  def typeProc(ast: RcModule): (RcModule, GlobalTable) = {
    val table = SymScanner(ast)
    val tyCtxt = TyCtxt()
    tyCtxt.setGlobalTable(table)
    val typedModule = TypedTranslator(tyCtxt)(ast)
    logf("typedModule.txt", typedModule)
    ClassesRender().rendClasses("classes.dot", "RcDump", typedModule.items collect { case i: Class => i })
    TypeCheck(typedModule)
    (typedModule, tyCtxt.globalTable)
  }

  def simplify(fn: Function) = {
    val pm = PassManager[Function]()
    val am = AnalysisManager[Function]()
    pm.addPass(CFGSimplify())
    pm.run(fn, am)
  }

  def dumpDomTree(fn: Function) = {
    simplify(fn)
    CFGRender.rendFn(fn, "whileBBs")
    val am = AnalysisManager[Function]()
    am.addAnalysis(DomTreeAnalysis())
    val domTree = am.getResult[DomTreeAnalysis](fn)
    println(domTree)
    //todo：rend一个支配树
    val loop = am.getResult[LoopAnalysis](fn)
  }

  def apply(option: CompileOption): Unit = {
    DumpManager.mkDumpRootDir
    val src = getSrc(option.srcPath)
    val ast = parse(src)
    val (typedModule, table) = typeProc(ast)
    val mirMod = log(ToMIR(table).proc(typedModule), "ToMIR")
    logf("mir.txt", mirMod)
//    dumpDomTree(mirMod.fnTable.values.head)
//    codegen(mirMod)
  }

  def codegen(mirMod: Module) = {
    val fns = log(toLIR(mirMod), "ToLIR")
    logf("LIR.txt", fns.mkString("\n\n"))
    genASM(fns)
    genELF(mirMod.fnTable.contains("main"))
  }


  def genELF(hasMain: Boolean) = {
    val o = log(as(DumpManager.getDumpRoot / "asm.s", DumpManager.getDumpRoot / "tmp.o"), "As")
    if (hasMain) {
      log(toExe(o.get), "ToELF")
    } else {
      warning("don't has main")
    }
  }

  def genASM(fns: List[MachineFunction]) = {
    val text = TextSection()
    val strTable = fns.flatMap(fn => (0 until fn.strTable.size).zip(fn.strTable.keys).map((i, str) => StrSection(i, List(str))))
    val rdata = RDataSection(strTable)
    fns.foreach(fn => text.addFn(fn.name -> fn.instructions.map(GNUASM.toASM)))
    val asm = text.asm + rdata.asm
    logf("asm.s", asm)
    asm
  }

  def as(srcPath: String, destPath: String): Option[String] = {
    val args = List(srcPath, "-o", destPath)
    val out = s"as ${args.mkString(" ")}".!!
    Some(destPath)
  }

  def toExe(asmPath: String) = {
    val outPath = asmPath.replace("tmp.o", "a.out")
    val args = List(asmPath, "-o", outPath)
    val out = s"gcc ${args.mkString(" ")}".!!
  }
}
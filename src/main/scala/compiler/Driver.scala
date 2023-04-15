package rclang
package compiler

import analysis.*
import ast.ClassesRender
import lexer.Lexer
import mir.*
import parser.RcParser
import pass.{AnalysisManager, PassManager, Transform}
import tools.*
import tools.RcLogger.{log, logf, warning}
import transform.CFGSimplify
import ty.*

import java.nio.file.{Files, Paths}
//import analysis.Analysis.`given`
import analysis.Analysis.{given_DomTreeAnalysis, given_LoopAnalysis}
import ast.{Class, Ident, Item, RcModule}
import codegen.*

import java.io.File
import java.nio.file.Path
import scala.io.Source
import scala.sys.process.*

object Driver {
  def getSrc(path: String) = {
    val f = Source fromFile path
    // avoid last line is end and lost last empty line
    val src = f.getLines.mkString("\n") + "\n"
    f.close()
    src
  }

  def parse(path: String): RcModule = {
    val src = getSrc(path)
    val tokens = log(Lexer(src).unwrap, "Lexer")
    logf("token.txt", tokens.mkString(" ").replace("EOL", "\n"))
    val module = log(RcParser(tokens).unwrap, "Parser").tap {
      logf("ast.txt", _)
    }
    module.copy(name = Paths.get(path).getFileName.toString)
  }

  def typeProc(ast: RcModule): (RcModule, GlobalTable) = {
    val table = SymScanner(ast)
    val tyCtxt = TyCtxt()
    tyCtxt.setGlobalTable(table)
    val typedModule = TypedTranslator(tyCtxt)(ast)
    logf("typedModule.txt", typedModule)
//    ClassesRender().rendClasses("classes.dot", DumpManager.getDumpRoot, typedModule.items collect { case i: Class => i })
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
    val loop = am.getResult[LoopAnalysis](fn)
  }

  def apply(option: CompileOption): Unit = {
    DumpManager.mkDumpRootDir
    val modules = option.srcPath.map(parse)
    dependencyResolve(modules)
    modules.foreach(compileAST)
  }

  def compileAST(module: RcModule): Unit = {
    val (typedModule, table) = typeProc(module)
    val mirMod = log(ToMIR(table).proc(typedModule), "ToMIR")
    //    logf("mir.txt", mirMod)
    //    dumpDomTree(mirMod.fnTable.values.head)
    codegen(mirMod)
  }

  def dumpPass(mf: MachineFunction, pass: Transform[MachineFunction]) = {
    val path = Paths.get(DumpManager.getDumpRoot / "Pass")
    if(!Files.exists(path)) {
      Files.createDirectories(path)
    }
    logf(f"Pass/${pass.getClass.getName.split('.').last}.txt") { writer =>
      MachineIRPrinter().printToWriter(mf, writer)
    }
  }

  def dumpFrameInfo(mf: MachineFunction): Unit = {
    logf("FrameInfo.txt", mf.frameInfo.toString)
  }

  def codegen(mirMod: Module) = {
    val translator = IRTranslator()
    val fns = translator.visit(mirMod.fns)
//    MachineIRPrinter().print(fns)
    val pm = PassManager[MachineFunction]()
    pm.addPass(new PhiEliminate())
    pm.addPass(new StackRegisterAllocation())
    pm.registerAfterPass(dumpPass)
    val am = AnalysisManager[MachineFunction]()
    fns.foreach(pm.run(_, am))
    fns.foreach(dumpFrameInfo)
    generateASM(fns, translator.strTable, DumpManager.getDumpRoot / "asm.s")
  }

  def genELF(hasMain: Boolean) = {
    val o = log(as(DumpManager.getDumpRoot / "asm.s", DumpManager.getDumpRoot / "tmp.o"), "As")
    if (hasMain) {
      log(toExe(o.get), "ToELF")
    } else {
      warning("don't has main")
    }
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

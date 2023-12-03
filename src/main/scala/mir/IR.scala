package rclang
package mir
import ty.{FnType, NilType, Type}

class BasicBlock(nameStr: String, var stmts: List[Instruction] = List()) extends Value with InFunction {
  name = nameStr
  stmts.foreach(inst => inst.parent = this)
  def terminator: Terminator = stmts.last.asInstanceOf[Terminator]

  // todo: this is append
  def insert[T <: Instruction](i: T): T = {
    stmts = stmts :+ i
    i
  }

  def successors = terminator.successors
  
  def preds: List[BasicBlock] = ???

  override def toString: String = s"BasicBlock:$name"
}

def bbToStr(bb: BasicBlock): String = {
  s"--- BasicBlock:${bb.name} ---\n${traverseInst(bb.stmts).mkString("\n")}"
}

case object Function {
  def Empty(name: String) = Function(name, NilType, List(), null, List())
}

case class Function(private val fnName: String,
                    var retType: Type,
                    var argument: List[Argument],
                    var entry: BasicBlock,
                    var bbs: List[BasicBlock] = List()) extends GlobalValue {
  name = fnName
  // todo: bad design
  if(entry != null) {
    entry.parent = this
  }

  var strTable = List[Str]()
  def instructions = bbs.flatMap(_.stmts)

  def fnType = FnType(retType, argument.map(_.ty))

  def getBB(name: String): BasicBlock = bbs.find(_.name == name).get

  override def toString: String = {
    val sign = s"$fnName(${argument.mkString(",")})\n"
    val body = s"{\n${bbs.map(bbToStr).mkString("\n")}\n}"
    sign + body
  }
}

case class Module(var name: String = "MainModule", var fnTable: Map[String, Function] = Map()) {
  var globalVariables: List[GlobalVariable] = List()
  var types: Set[Type] = Set()
  var context: RcContext = null
  def fns = fnTable.values.toList

  override def toString: String = name + "\n" + fnTable.values.map(_.toString).mkString("\n\n")
}

case class RcContext() {
  var modules: List[Module] = List()
}

case class Loop(var bbs: List[BasicBlock], var parentLoop: Loop = null, var subLoop: List[Loop] = List()) {
  // header is compare
  def header = bbs.head
//  def body: BasicBlock
//  def latch: BasicBlock
//  def exit: BasicBlock
}

case class LoopInfo(var loops: Map[BasicBlock, Loop] = Map())
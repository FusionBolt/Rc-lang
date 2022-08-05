package rclang
package mir
import ty.{FnType, Type}

class BasicBlock(nameStr: String, var stmts: List[Instruction] = List()) extends Value with InFunction {
  name = nameStr
  def terminator: Terminator = stmts.last.asInstanceOf[Terminator]

  def insert[T <: Instruction](i: T): T = {
    stmts = stmts :+ i
    i
  }

  def successors = terminator.successors

  override def toString: String = s"BasicBlock:$name"
}

case class Function(fnName: String,
                    var retType: Type,
                    var argument: List[Argument],
                    var bbs: List[BasicBlock] = List()) extends GlobalValue {
  name = fnName
  var entry: BasicBlock = null
  def instructions = bbs.flatMap(_.stmts)

  def fnType = FnType(retType, argument.map(_.ty))

  def getBB(name: String): BasicBlock = bbs.find(_.name == name).get

  // todo: dump with basicblock
  override def toString: String = s"$fnName(${argument.mkString(",")})\n${traverseInst(instructions).mkString("\n")}\n"
}

case class Module(var name: String = "", var fnTable: Map[String, Function] = Map()) {
  var globalVariables: List[GlobalVariable] = List()
  var types: Set[Type] = Set()
  var context: RcContext = null
  def fns = fnTable.values

  override def toString: String = name + "\n" + fnTable.values.map(_.toString).mkString("\n")
}

case class RcContext() {
  var modules: List[Module] = List()
}
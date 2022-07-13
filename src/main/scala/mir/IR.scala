package rclang
package mir
import ty.{FnType, Type}

case class BasicBlock(nameStr: String, var stmts: List[Instruction] = List()) extends Value with InFunction {
  name = nameStr
  def terminator: Terminator = stmts.last.asInstanceOf[Terminator]

  def insert[T <: Instruction](i: T): T = {
    stmts = stmts :+ i
    i
  }

  def prev = parent.bbs(parent.bbs.indexOf(this))

  def successors = terminator.successors

  override def toString: String = "BasicBlock"
}

case class Function(fnName: String,
                    var retType: Type,
                    var argument: List[Argument],
                    var bbs: List[BasicBlock] = List()) extends GlobalValue {
  name = fnName
  var entry: BasicBlock = null
  def instructions = bbs.flatMap(_.stmts)

  def fnType = FnType(retType, argument.map(_.ty))
}

case class Module(var name: String = "", var fnTable: Map[String, Function] = Map()) {
  var globalVariables: List[GlobalVariable] = List()
  var types: Set[Type] = Set()
  var context: RcContext = null
}

case class RcContext() {
  var modules: List[Module] = List()
}
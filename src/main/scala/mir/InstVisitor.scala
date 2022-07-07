package rclang
package mir


def traverse[T, U](list: List[T], f: T => U): List[U] =
  list.map(f)

// todo:check instruction(Type and other)
def printInst[T <: Instruction](list: List[T]): List[Unit] =
  traverse(list, println)

trait InstVisitor {
  type TRet = Unit
  def visit(inst: Instruction): TRet = {
    inst match
      case i: Call => visit(i)
      case i: Call => visit(i)
      case i: CondBranch => visit(i)
      case i: Branch => visit(i)
      case i: Return => visit(i)
      case i: Binary => visit(i)
      case i: Alloc => visit(i)
      case i: Load => visit(i)
      case i: Store => visit(i)
      case i: PhiNode => visit(i)
  }

  def visit(call: Call): TRet = {

  }

  def visit(condbranch: CondBranch): TRet = {

  }
  def visit(branch: Branch): TRet = {

  }
  def visit(ret: Return): TRet = {

  }
  def visit(binary: Binary): TRet = {

  }
  def visit(alloc: Alloc): TRet = {

  }
  def visit(load: Load): TRet = {

  }
  def visit(store: Store): TRet = {

  }
  def visit(phinode: PhiNode): TRet = {

  }
}

class Printer{
  def visit(inst: Instruction): Unit = {
    inst match {
      case l:Load => "load"
      case s:Store => "store"
      case _ =>
    }
  }
}

trait MIRVisitor extends InstVisitor {
  def visit(fn: Function): TRet = {
    fn.instructions.foreach(visit)
  }
}

class MIRPrinter extends MIRVisitor {
  override def visit(call: Call): TRet = {

  }

  override def visit(condbranch: CondBranch): TRet = {

  }

  override def visit(branch: Branch): TRet = {

  }

  override def visit(ret: Return): TRet = {

  }

  override def visit(binary: Binary): TRet = {

  }

  override def visit(alloc: Alloc): TRet = {

  }

  override def visit(load: Load): TRet = {
    println("load")
  }

  override def visit(store: Store): TRet = {

  }

  override def visit(phinode: PhiNode): TRet = {

  }
}
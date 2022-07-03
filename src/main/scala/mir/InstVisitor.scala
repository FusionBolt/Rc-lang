package rclang
package mir


def traverse[T, U](list: List[T], f: T => U): List[U] =
  list.map(f)

def traverseInst[T <: Instruction](list: List[T]): List[Unit] =
  traverse(list, i => i match {
    case i: Call => println("Call")
    case i: CondBranch => println("CondBranch")
    case i: Branch => println("Branch")
    case i: Return => println("Return")
    case i: Binary => println("Binary")
    case i: Alloc => println("Alloc")
    case i: Load => println("Load")
    case i: Store => println("Store")
    case i: PHINode => println("PHINode")
    case _ =>
  })

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
      case i: PHINode => visit(i)
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
  def visit(phinode: PHINode): TRet = {

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

  override def visit(phinode: PHINode): TRet = {

  }
}
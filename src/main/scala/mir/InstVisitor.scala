package rclang
package mir


def traverse[T, U](list: List[T])(f: T => U): List[U] =
  list.map(f)

def traverseInst[T <: Instruction](list: List[T]): List[String] =
  traverse(list)(inst => {
    Printer().visit(inst)
    //    val user = inst.asInstanceOf[User]
    //    s"${inst.getClass.getSimpleName}:${user.ty} ${user.operands.map(_.toString).mkString(" ")}"
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
  def opsToString(user: User) = {
    user.operands.map(_.toString).mkString(" ")
  }

  def instName(inst: Instruction) = {
    inst.getClass.getSimpleName
  }

  def visit(inst: Instruction): String = {
    val user = inst.asInstanceOf[User]
    inst match {
      //      case BinaryInstBase(lhsValue, rhsValue) => ???
      //      case UnaryInst(operandValue) => ???
      case intrinsic: Intrinsic => s"${instName(inst)} ${intrinsic.name}: ${user.ty}"
      //      case CondBranch(condValue, tBranch, fBranch) => ???
      //      case Branch(destBasicBlock) => ???
      //      case Return(value) => ???
      case bn @ Binary(op, lhs, rhs) => s"${instName(inst)}: ${user.ty} $op(${lhs}, ${rhs})"
      //      case Alloc(id, typ) => ???
      //      case Load(ptr) => ???
//      case st @ Store(value, ptr) => s"${instName(inst)}: ${user.ty} ${st.value} -> ${st.ptr}"
      case st: Store => s"${instName(inst)}: ${user.ty} ${st.value} -> ${st.ptr}"
      //      case GetElementPtr(value, offset) => ???
      //      case PhiNode(incomings) => ???
      //      case SwitchInst() => ???
      //      case MultiSuccessorsInst(bbs) => ???
      case _ => s"${instName(inst)}:${user.ty} ${opsToString(user)}"
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
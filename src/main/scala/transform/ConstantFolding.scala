package rclang
package transform

import mir.ImplicitConversions.*
import mir.{InstVisitor, *}
import pass.{AnalysisManager, Transform}

import scala.math
import scala.math.Fractional.Implicits.infixFractionalOps
import scala.math.Integral.Implicits.infixIntegralOps
import scala.math.Numeric.Implicits.infixNumericOps


//def isConstant(v: Value): Boolean = {
//  v match
//    case block: BasicBlock => isConstant(block.stmts.last)
//    case value: GlobalValue => ???
//    case user: User => user match
//      case constant: Constant => true
//      case instruction: Instruction => instruction.getOperands.forall(isConstant)
//      case _ => ???
//    case _ => false
//}
//
//def isConstant(inst: Instruction): Boolean = {
//  inst match
//    case UnaryInst(operandValue) => isConstant(operandValue)
//    case base: CallBase => ???
//    case intrinsic: Intrinsic => ???
//    // isConstant(condValue), cond -> isConstant(branch)
//    case CondBranch(condValue, tBranch, fBranch) => ???
//    case Branch(destBasicBlock) => false
//    case Return(retValue) => isConstant(retValue)
//    case Binary(op, lhs_value, rhs_value) => isConstant(lhs_value) && isConstant(rhs_value)
//    case Alloc(id, typ) => false
//    case Load(valuePtr) => isConstant(valuePtr)
//    case Store(value, ptr) => isConstant(value)
//    case GetElementPtr(value, offset, targetTy) => false
//    case PhiNode(incomings) => false
//    case SwitchInst() => ???
//    case MultiSuccessorsInst(bbs) => ???
//    case _ => ???
//}

def eval(value: Value): Option[Value] = {
  value match
    case Argument(nameStr, argTy) => None
    case block: BasicBlock => ???
    case value: GlobalValue => ???
    case user: User => user match
      case constant: Constant => None
      case instruction: Instruction => evalInst(instruction)
      case _ => ???
    case _ => ???
}

/** return a inst maybe change
 **/
def evalInst(inst: Instruction): Option[Value] = {
  inst match
    case UnaryInst(operandValue) => ???
    case Return(retValue) => eval(retValue).map(Return)
    case bn: Binary => foldBinaryInstruction(bn)
    case Load(valuePtr) => eval(valuePtr).map(Load)
    case Store(value, ptr) => eval(value).map(Store(_, ptr))
    case alloc: Alloc => {
      val stores = alloc.operands.filter(use => use.parent.isInstanceOf[Store])
      // store only once
      if (stores.size == 1) {
        val store = stores.head.parent.asInstanceOf[Store]
        evalInst(store)
      } else {
        None
      }
    }
    case _ => None
//    case base: CallBase => ???
//    case intrinsic: Intrinsic => ???
//    case CondBranch(condValue, tBranch, fBranch) => ???
//    case GetElementPtr(value, offset, targetTy) => ???
//    case PhiNode(incomings) => ???
//    case Branch(destBasicBlock) => ???
//    case SwitchInst() => ???
//    case MultiSuccessorsInst(bbs) => ???
}

def foldBinaryOp(op: String, v: Value, c: Constant, bn: Binary): Option[Value] = {
  op match
    case _ @("add" | "sub") if c.isZero => Some(v)
    case _ @("mul" | "div") if c.isOne => Some(v)
    case _ => None
}

def getDouble(n: Number): Double = {
  n match
    case Integer(value) => value
    case FP(value) => value
    case _ => ???
}

def compute(op: String, a: Number, b: Number): Number = {
  val lhs = getDouble(a)
  val rhs = getDouble(b)
  val result = op match
    case "Add" => lhs + rhs
    case "Sub" => lhs - rhs
    case _ => ???
  a match
    case Integer(value) => Integer(result.toInt)
    case FP(value) => FP(result.toInt)
    case _ => ???
}

def foldBinaryInstruction(bn: Binary): Option[Value] = {
  (bn.lhs, bn.rhs) match
    case (lhs: Constant, rhs: Constant) => (lhs, rhs) match
      case (a: Integer, b: Integer) => Some(compute(bn.op, a.value, b.value))
      case (a: FP, b: FP) => Some(compute(bn.op, a.value, b.value))
      case _ => ???
    case (lhs: Constant, rhs: Value) => foldBinaryOp(bn.op, rhs, lhs, bn)
    case (lhs: Value, rhs: Constant) => foldBinaryOp(bn.op, lhs, rhs, bn)
    case (lhs: Value, rhs: Value) => None
}

// 1. operand is constant && operator can be eval
// 2. eval
// 3. replace -> find uses
class ConstantFolding extends Transform[Function] {
  // binary -> x +- 0, x */ 1
  override def run(iRUnitT: Function, AM: AnalysisManager[Function]): Unit = {
    println(iRUnitT)
    traverse(iRUnitT.instructions)(inst => {
        evalInst(inst) match
          case Some(after) => {
            println("replace")
            println(inst)
            println(after)
            assert(inst != after)
            println("--")
            inst.replaceAllUseWith(after)
          }
          case None =>
    })
//    workList.foreach(inst => inst.replaceAllUseWith(eval(inst)))
  }
}

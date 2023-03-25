package rclang
package codegen

abstract class TargetMachine() {
  val cpu: String
  val regInfos: List[RegInfo]
  val callingConvention: CallingConvention
  val wordSize: Int
  val gregCount: Int
  val asmEmiter: ASMEmiter
}

enum CallingConvention:
  case x86
  case x86_64

case class RegInfo(name: String, asmName: String, alias: Set[String], id: Int, bit: Int = 4)
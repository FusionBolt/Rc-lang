package rclang
package codegen

enum CallingConvention:
  case x86
  case x86_64

import CallingConvention.*

trait TargetMachine {
  val cpu = "general"
  val callingConvention = x86_64
  val wordSize = if callingConvention == x86_64 then 8 else 4
  val gregCount = 16
  val assembler: Assembler
}

trait Assembler {

}

// extend a visitor
trait Legalizer {
  def legalize(module: Module) = {
    module
  }
}
package rclang
package codegen

import scala.collection.immutable.ListMap

def mkInst(name: String, fields: List[(String, Int)]): ISAInst = {
  ISAInst()
}

def inst(name: String)(fields: (String, Int)*)(using isa: ISA): ISAInst = {
  val inst = mkInst(name, fields.toList)
  isa.addInst(inst)
  inst
}

def init: ISA = {
  var isa = ISA()
  given ISA = isa
  inst("load")(
    "rd" -> 5,
    "funct3" -> 3,
    "rs1" -> 5,
    "imm" -> 12
  )
  inst("STORE")(
    "offset_4_0" -> 5,
    "width" -> 3,
    "base" -> 5,
    "src" -> 5,
    "offset" -> 5)
  inst("ADDIW")(
    "rd" -> 5,
    "funct" -> 3,
    "rs" -> 1,
    "imm_11_0" -> 12)
  isa
}


def isaCheck(): Boolean = {
  val isa = init
  val rvInstLength = 32
  isa.instSet.forall(_.fields.values.sum == rvInstLength)
}
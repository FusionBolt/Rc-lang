package rclang
package ty

import ast.{ASTNode, Class}

import rclang.tools.GlobalTable

import collection.immutable.ListMap

sealed class Type

case object BooleanType extends Type

case object StringType extends Type

case object Int32Type extends Type

case object Int64Type extends Type

case object FloatType extends Type

case object NilType extends Type

case class FnType(ret: Type, params: List[Type]) extends Type

case object InferType extends Type

case class ArrayType(valueT: Type) extends Type

case class ErrType(msg: String) extends Type

case object TypeBuilder {
  def fromClass(klass: Class, gt: GlobalTable): StructType = {
    val name = klass.name
    fromClass(name.str, gt)
  }

  def fromClass(name: String, gt: GlobalTable): StructType = {
    val vars = gt.classTable(name).allInstanceVars(gt)
    StructType(name, ListMap.from(vars.map(field => field.name.str -> Infer.translate(field.ty))))
  }
}

case class StructType(name: String, private var fields: ListMap[String, Type]) extends Type {
  def align = fieldSizes.min

  def fieldOffset(field: String) = {
    // 1. find index
    // 2. reduce to index
    fields.zipWithIndex.find(_._1._1 == field) match
      case Some(value) => fields.slice(0, value._2).values.map(sizeof).sum
      case None => ???
  }

  def fieldSizes = fields.values.map(sizeof)

  def sizeAfterAlign(align: Int) = {
    fieldSizes.map(size => (size / align + 1) * align)
  }

  override def toString: String = s"StructType($name)"
}

case class PointerType(ty: Type) extends Type

def sizeof(ty: Type): Int = {
  // PtrLength == WordLength
  val ptrLength = 8
  ty match
    case BooleanType => 1
    case StringType => ptrLength
    case Int32Type => 4
    case FloatType => 4
    case NilType => 0
    case FnType(ret, params) => ptrLength
    case InferType => -1 // enum
    case ErrType(msg) => -1
    case StructType(name, fields) => -1
    case PointerType(ty) => ptrLength
    case _ => ???
}

trait Typed {
  var ty: Type = InferType

  def withTy(ty: Type): this.type = {
    this.ty = ty
    this
  }

  def withInfer: this.type = withTy(infer)

  def infer: Type = Infer(this)
}
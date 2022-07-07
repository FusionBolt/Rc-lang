package rclang
package ty

import ast.ASTNode

sealed class Type

case object BooleanType extends Type

case object StringType extends Type

case object Int32Type extends Type

case object FloatType extends Type

case object NilType extends Type

case class FnType(ret: Type, params: List[Type]) extends Type

case object InferType extends Type

case class ErrType(msg: String) extends Type

case class StructType(name: String, fields: Map[String, Type]) extends Type

trait Typed {
  var ty: Type = InferType

  def withTy(ty: Type): this.type = {
    this.ty = ty
    this
  }

  def withInfer: this.type = withTy(infer)

  def infer: Type = Infer(this)
}
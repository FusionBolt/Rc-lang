package rclang
package ty

import ast.ASTNode

enum Type:
  case Boolean
  case String
  case Int32
  case Float
  case Nil
  case Fn(ret: Type, params: List[Type])
  case Infer
  case Err(msg: String)

trait Typed {
  var ty:Type = Type.Infer

  def withTy(ty: Type): this.type = {
    this.ty = ty
    this
  }

  def withInfer: this.type = withTy(infer)

  def infer: Type = Infer(this)
}
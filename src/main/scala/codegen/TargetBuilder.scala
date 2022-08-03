package rclang
package codegen

class TargetBuilder(name: String) {

}

// extend a visitor
trait Legalizer {
  def legalize(module: Module) = {
    module
  }
}
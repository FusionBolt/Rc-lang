package rclang
package codegen

trait Assembly {
  def file: Unit
  def section: Unit
  def global: Unit
  def label: Unit
  def comment: Unit

  def serialize: String = {
    s".file \"${file}\""
  }
}

package rclang
package tools

object Debugger {
  def check(valid : => Boolean, msg: String) = {
    if(!valid) {
      throw new RuntimeException(msg)
    }
  }
}

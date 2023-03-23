package rclang
package tools

trait In[T] {
  var parent: T = null.asInstanceOf[T]
}
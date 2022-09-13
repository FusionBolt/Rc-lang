package rclang
package tools

class State[T](var value: T) {

  /** switch to a tmp state and call f
   * @param newState
   * @param f
   * @return: value before restore state
   */
  def by(newState: T)(f:() => Unit) = {
    val oldValue = value
    value = newState
    f()
    val save = value
    value = oldValue
    save
  }
}

implicit def toState[T](v: T): State[T] = {
  new State(v)
}

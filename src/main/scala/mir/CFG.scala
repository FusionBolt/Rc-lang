package rclang
package mir

def canReach(a: BasicBlock, b: BasicBlock): Boolean = {
  if (a == b) return true
  a.successors.exists(canReach(_, b))
}
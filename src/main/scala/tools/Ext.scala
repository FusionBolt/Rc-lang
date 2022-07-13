package rclang
package tools

extension [T](x: T) {
  def tap(f: T => Unit): T = { f(x); x }
}

def run[TL, TR](result: => Either[TL, TR]): TR = {
  result match {
    case Left(l) => throw new RuntimeException(l.toString)
    case Right(r) => r
  }
}

extension [TL, TR](result: => Either[TL, TR]) {
  def unwrap: TR = {
    result match {
      case Left(l) => throw new RuntimeException(l.toString)
      case Right(r) => r
    }
  }
}
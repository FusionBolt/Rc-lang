package rclang

sealed trait RcCompilationError

case class RcLexerError(location: Location, msg: String) extends RcCompilationError
case class RcParserError(location: Location, msg: String) extends RcCompilationError
case class RcNotSupported(location: Location, msg: String) extends RcCompilationError

case class Location(line: Int, column: Int) {
  override def toString = s"$line:$column"
}
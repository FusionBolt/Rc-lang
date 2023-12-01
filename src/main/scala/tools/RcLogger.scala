package rclang
package tools

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, IOApp}
import io.odin.*
import cats.effect.IO
import cats.effect.unsafe.IORuntime

import java.io.{File, PrintWriter}


object RcLogger {
  val logger: Logger[IO] = consoleLogger()

  def warning(str: String): Unit = {
    log(str)
  }

  def log(str: String): Unit = {
    logger.info(str).unsafeRunSync()
  }

  def log[T](result: T, stage: String): T = {
    val r = result
    log(stage + " Finish")
    r
  }

  def logf[T](path: String, v: T): Unit = logf(path, v.toString)

  def logf(path: String, str: String): Unit = {
    logf(path)(_.write(str))
  }

  def logf(path: String)(f: PrintWriter => Unit) = {
    val printer = new PrintWriter(new File(DumpManager.getDumpRoot, path));
    f(printer)
    printer.close()
  }

  def logSep(prefix: String, f: => Unit) = {
    log(s"------------$prefix begin------------")
    f
    log(s"------------$prefix end------------")
  }
  var level = 2
}
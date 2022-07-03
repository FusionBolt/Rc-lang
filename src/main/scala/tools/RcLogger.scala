package rclang
package tools

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, IOApp}
import io.odin.*
import cats.effect.IO
import cats.effect.unsafe.IORuntime

import java.io.{File, PrintWriter}


object RcLogger extends IOApp.Simple {
  val logger: Logger[IO] = consoleLogger()

  def log(str: String): Unit = {
    logger.info(str).unsafeRunSync()
  }

  def log[T](result: T, stage: String): T = {
    val r = result
    log(stage + " Finish")
    r
  }

  def log[T](path: String, f: PrintWriter => Unit) = {
    val printer = new PrintWriter(new File(DumpManager.dumpRoot, path));
    f(printer)
    printer.close()
  }

  def run: IO[Unit] = {
    logger.info("")
  }

  var level = 1
}
package rclang
package tools

import java.nio.file.{Files, Paths}

object DumpManager {
  private var dumpRoot = "RcDump"
  def mkDumpRootDir = {
    Files.createDirectories(Paths.get(dumpRoot))
  }

  def setDumpRoot(path: String) = {
    dumpRoot = path
    mkDumpRootDir
  }

  def getDumpRoot = dumpRoot
}

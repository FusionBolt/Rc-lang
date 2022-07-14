package rclang

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.should.Matchers
import java.io.File
import tools.{DumpManager, RcLogger}

class RcTestBase extends AnyFunSpec with BeforeAndAfter with Matchers {
  DumpManager.setDumpRoot("RcTestDump")
}

package xand

import scala.io.Source
import scala.util.Try

import xand.rendering.{Snapshot, Fetch, Sub, Branch, Continue}

class Simulator extends VM {
  override def run() {
    try { super.run() }
    catch { case Halt => Snapshot.render(this) }
  }

  override def fetch() {
    super.fetch()
    Fetch.render(this)
  }

  override def sub() {
    super.sub()
    Sub.render(this)
  }

  override def branch() {
    super.branch()
    Branch.render(this)
  }

  override def continue() {
    super.continue()
    Continue.render(this)
  }
}

object Simulator {
  def main(args: Array[String]) {
    val source = args match {
      case Array() => Source.fromURL(getClass.getResource("fib.xand"))
      case Array(filename, _*) => Source.fromFile(filename)
    }
    val bytecode = Compiler.compile(source.mkString).get
    val simulator = new Simulator
    simulator.load(bytecode)
    simulator.run()
  }
}

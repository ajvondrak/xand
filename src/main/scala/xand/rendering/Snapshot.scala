package xand
package rendering

import Colors.Color
import sys.process._

class Snapshot(vm: VM) {
  implicit class FormattedByte(byte: Byte) {
    def formatted: String = {
      val leastSignificantBits = Integer.toBinaryString(byte & 0xff)
      val bits = "%8s".format(leastSignificantBits).replace(' ', '0')
      "%s (%+4d)".format(bits, byte)
    }
  }

  trait Highlighting {
    protected var foreground: Color = Colors.Default
    protected var background: Color = Colors.Default

    def highlighting: Color = foreground on background

    def highlight(color: Color) {
      background = foreground
      foreground = color
    }
  }

  class ProgramCounter(val address: Byte) extends Highlighting {
    override def toString: String =
      (address.formatted paint highlighting).toString
  }

  class InstructionRegister(value: (Byte, Byte, Byte)) {
    val minuend: Byte = value._1
    val subtrahend: Byte = value._2
    val branch: Byte = value._3

    override def toString: String = {
      val a = value._1.formatted on Colors.minuend
      val b = value._2.formatted on Colors.subtrahend
      val c = value._3.formatted on Colors.branch
      s"$a | $b | $c"
    }
  }

  class Memory(contents: Array[Byte]) {
    class Cell(addressedByte: (Byte, Int)) extends Highlighting {
      def byte: Byte = addressedByte._1
      def address: Int = addressedByte._2

      private var pointer: Char = ' '
      def point { pointer = '>' }

      override def toString: String =
        s"$pointer%4d: %s".format(
          address,
          byte.formatted paint highlighting
        )
    }

    val cells: Array[Cell] = contents.zipWithIndex.map(new Cell(_))
    val columns: Array[Array[Cell]] = cells.grouped(32).toArray
    val rows: Array[Array[Cell]] = columns.transpose

    override def toString: String =
      rows.map(_.mkString(" | ")).mkString("\n", "\n", "")

    def pointAt(pc: ProgramCounter) { cells(pc.address).point }
  }

  val pc = new ProgramCounter(vm.pc)
  val ir = new InstructionRegister(vm.ir)
  val memory = new Memory(vm.memory)

  memory.pointAt(pc)

  def render() {
    clear()
    println(s" PC: $pc")
    println(s" IR: $ir")
    println(s"MEM: $memory")
    pause()
  }

  def pause() { Thread.sleep(500L) }

  /* Just doing a straight "clear".! or printing the ANSI "move cursor to top
   * of screen, erase contents" code (i.e., "\033[H\033[2J") results in choppy
   * output in URXVT. Presumably it has to do with the blip from erasing the
   * content, then flushing the new content into the terminal. I don't really
   * know.
   *
   * But aside from that, literally overwriting the old content makes it hard
   * to debug programs, because you can't just look at the scrollback and see
   * the sequence of states.
   *
   * Thus, I emulate the `clear` / ctrl-L behavior you get in other VTs by
   * making a call to `tput` to find out how many newlines we have to print
   * out. I presume (hope) this works in terminals other than mine.
   */

  def clear() { print("\n" * lines) }

  private def lines: Int = "tput lines".!!.trim.toInt
}

object Snapshot {
  def render(vm: VM) { (new Snapshot(vm)).render() }
}

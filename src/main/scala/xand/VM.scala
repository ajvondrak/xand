package xand

class VM {
  object Halt extends scala.util.control.ControlThrowable

  /* Program Counter */
  var pc: Byte = 0

  /* Instruction Register */
  var ir: (Byte, Byte, Byte) = (0, 0, 0)

  /* Memory */
  val memory: Array[Byte] = new Array(Byte.MaxValue + 1)

  def load(bytecode: Bytecode) {
    require(bytecode.size <= memory.size, "Bytecode will not fit in memory.")
    for (i <- bytecode.indices) memory(i) = bytecode(i)
  }

  def run() { while (true) step() }

  protected def step() {
    fetch()
    xand() /* = decode + execute, since there's no decoding */
  }

  protected def fetch() {
    if (pc < 0 || pc + 2 >= memory.size) throw Halt
    ir = (memory(pc), memory(pc + 1), memory(pc + 2))
    if (ir._1 < 0 || ir._2 < 0 || ir._3 < 0) throw Halt
  }

  protected def xand() {
    sub()
    blez()
  }

  protected def sub() {
    memory(ir._1) = (memory(ir._1) - memory(ir._2)).toByte
  }

  protected def blez() {
    if (memory(ir._1) <= 0) branch() else continue()
  }

  protected def branch() {
    pc = ir._3
  }

  protected def continue() {
    pc = (pc + 3).toByte
  }
}

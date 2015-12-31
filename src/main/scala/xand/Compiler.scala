package xand

import scala.util.Try

object Compiler {
  def compile(code: String): Try[Bytecode] =
    for {
      program <- Parser.parse(code)
    } yield (new CompilationUnit(program)).bytecode

  final object ProgramTooLong
    extends Exception("Bytecode will not fit in memory.")

  final class UnknownLabel(label: String)
    extends Exception(s"Unknown label: `$label'.")

  private class CompilationUnit(program: Program) {
    val addresses = new Iterator[Byte] {
      private var address: Byte = 0
      private val instructions: Iterator[Instruction] = program.iterator

      def hasNext: Boolean = instructions.hasNext

      def next(): Byte = {
        if (address < 0) throw ProgramTooLong
        val current = address
        instructions.next() match {
          case _: Data => address = (address + 1).toByte
          case _: Xand => address = (address + 3).toByte
        }
        current
      }
    }

    val memoryMap: Seq[(Byte, Instruction)] =
      addresses.zip(program.iterator).toSeq

    val symbolTable: Map[String, Byte] = memoryMap.collect({
      case (address, Data(Some(label), _)) => label -> address
      case (address, Xand(Some(label), _, _, _)) => label -> address
    }).toMap

    def resolveAt(base: Byte)(address: Address): Byte = address match {
      case ResolvedAddress(byte) => byte
      case UnresolvedAddress("...") => (base + 3).toByte
      case UnresolvedAddress(label) => symbolTable.getOrElse(label, {
        throw new UnknownLabel(label)
      })
    }

    def emit(address: Byte, instruction: Instruction): Bytecode =
      instruction match {
        case Xand(_, a, b, c) => Seq(a, b, c) map resolveAt(address)
        case Data(_, literal) => Seq(literal)
      }

    def bytecode: Bytecode = memoryMap flatMap Function.tupled(emit)
  }
}

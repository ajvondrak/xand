package xand

import scala.util.Try
import scala.util.control.Exception._
import scala.util.parsing.combinator._

object Parser extends RegexParsers {
  val byte: Parser[Byte] = """-?\d+""".r ^? (
    Function.unlift { catching(classOf[NumberFormatException]) opt _.toByte },
    b => s"`$b' is not a valid a signed byte"
  )

  val label: Parser[String] = not("xand") ~> "[a-z_][a-z0-9_]*".r

  val address: Parser[Address] =
    byte ^^ { ResolvedAddress(_) } | label ^^ { UnresolvedAddress(_) }

  val branch: Parser[Address] =
    address | "..." ^^ { UnresolvedAddress(_) }

  val xand: Parser[Xand] =
    (opt(label <~ ":") <~ "xand") ~ address ~ address ~ branch ^^ {
      case label ~ a ~ b ~ c => Xand(label, a, b, c)
    }

  val data: Parser[Data] = opt(label <~ ":") ~ byte ^^ {
    case label ~ value => Data(label, value)
  }

  val instruction: Parser[Instruction] = xand | data

  val program: Parser[Program] = rep1(instruction)

  class ParseException(failure: NoSuccess) extends Exception(failure.toString)

  def parse(code: String): Try[Program] = Try {
    parseAll(program, code) match {
      case Success(instructions, _) => instructions
      case failure: NoSuccess => throw new ParseException(failure)
    }
  }
}

package xand

import scala.util.control.Exception._
import scala.util.parsing.combinator._
import xand.ast._

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

  val program: Parser[Seq[Expression]] = rep1(xand | data)

  def apply(in: String) = parseAll(program, in) match {
    case Success(result, _) => result
    case failure: NoSuccess => println(failure)
  }
}

package xand

package object ast {
  type Label = Option[String]

  sealed abstract class Expression(label: Label = None)

  case class Data(
    label: Label = None,
    value: Byte
  ) extends Expression(label)

  case class Xand(
    label: Label = None,
    minuend: Address,
    subtrahend: Address,
    branch: Address
  ) extends Expression(label)

  sealed abstract trait Address
  case class ResolvedAddress(value: Byte) extends Address
  case class UnresolvedAddress(label: String) extends Address
}

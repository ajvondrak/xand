package xand

sealed abstract trait Address
case class ResolvedAddress(value: Byte) extends Address
case class UnresolvedAddress(label: String) extends Address

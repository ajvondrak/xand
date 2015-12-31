package xand

sealed abstract class Instruction(label: Label = None)

case class Data(
  label: Label = None,
  value: Byte
) extends Instruction(label)

case class Xand(
  label: Label = None,
  minuend: Address,
  subtrahend: Address,
  branch: Address
) extends Instruction(label)

package xand

package object rendering {
  implicit def colorize[T](value: T): Colorful[T] = new Colorful(value)
  implicit def decolorize[T](c: Colorful[T]): T = c.value
}

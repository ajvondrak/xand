package xand
package rendering

import Colors.{Color, Default}

class Colorful[A](val value: A, val color: Color = Default) {
  def paint(newColor: Color): Colorful[A] =
    new Colorful(value, newColor)

  def colored(foreground: Color): Colorful[A] =
    paint(foreground on color)

  def on(background: Color): Colorful[A] =
    paint(color on background)

  override def toString: String =
    color + value.toString + scala.io.AnsiColor.RESET
}

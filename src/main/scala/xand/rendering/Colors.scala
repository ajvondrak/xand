package xand
package rendering

object Colors extends Enumeration {
  import scala.io.AnsiColor._

  case class Color(val foreground: String, val background: String) {
    override def toString: String = foreground + background
    def on(bg: Color): Color = Color(foreground, bg.background)
  }

  type Value = Color

  val Default = Color("", "")
  val Black   = Color(BLACK, BLACK_B)
  val Blue    = Color(BLUE, BLUE_B)
  val Cyan    = Color(CYAN, CYAN_B)
  val Green   = Color(GREEN, GREEN_B)
  val Magenta = Color(MAGENTA, MAGENTA_B)
  val Red     = Color(RED, RED_B)
  val White   = Color(WHITE, WHITE_B)
  val Yellow  = Color(YELLOW, YELLOW_B)

  def minuend: Color = Red
  def subtrahend: Color = Green
  def branch: Color = Blue
  def delta: Color = Yellow
}

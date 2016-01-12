package xand
package rendering

class Fetch(vm: VM) extends Snapshot(vm) {
  memory.cells(ir.minuend) highlight Colors.minuend
  memory.cells(ir.subtrahend) highlight Colors.subtrahend
}

object Fetch {
  def render(vm: VM) { (new Fetch(vm)).render() }
}

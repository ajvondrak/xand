package xand
package rendering

class Sub(vm: VM) extends Snapshot(vm) {
  memory.cells(ir.minuend) highlight Colors.delta
}

object Sub {
  def render(vm: VM) { (new Sub(vm)).render() }
}

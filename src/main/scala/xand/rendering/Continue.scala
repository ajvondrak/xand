package xand
package rendering

class Continue(vm: VM) extends Snapshot(vm) {
  pc highlight Colors.delta
}

object Continue {
  def render(vm: VM) { (new Continue(vm)).render() }
}

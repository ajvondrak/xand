package xand
package rendering

class Branch(vm: VM) extends Snapshot(vm) {
  pc highlight Colors.branch
}

object Branch {
  def render(vm: VM) { (new Branch(vm)).render() }
}

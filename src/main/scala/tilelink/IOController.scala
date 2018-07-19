// See LICENSE.SiFive for license details.

package freechips.rocketchip.tilelink

import Chisel._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.subsystem._

abstract class TLIOController[T <: Data](
     val base:        BigInt,
     val devname:     String,
     val devcompat:   Seq[String],
     val nInterrupts: Int     = 0,
     val size:        BigInt  = 4096,
     val concurrency: Int     = 0,
     val beatBytes:   Int     = 4,
     val undefZero:   Boolean = true,
     val executable:  Boolean = false,
     val crossingType: SubsystemClockCrossing = SynchronousCrossing(BufferParams.none))
   (portBundle: T)(implicit p: Parameters)
  extends TLRegisterRouterBase(devname, devcompat, AddressSet(base, size-1), nInterrupts, concurrency, beatBytes, undefZero, executable)
  with LazyScope {

  val ioNode = BundleBridgeSource(() => portBundle.cloneType)
  protected val tlControlXingNode = new CrossingHelper(this, crossingType)
  protected val intXingNode = new CrossingHelper(this, crossingType)
  def crossControlIn(implicit p: Parameters): TLNode = node := tlControlXingNode.crossTLIn
  def crossIntOut(implicit p: Parameters): IntNode = intXingNode.crossIntOut := intnode

  val port = InModuleBody { ioNode.out.head._1 }
  val interrupts = InModuleBody { if (intnode.out.isEmpty) Vec(0, Bool()) else intnode.out(0)._1 }
}

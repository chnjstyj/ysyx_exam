import circt.stage._

object Elaborate extends App {
  def top = new top(22,5,5,2)
  val useMFC = false // use MLIR-based firrtl compiler
  val generator = Seq(chisel3.stage.ChiselGeneratorAnnotation(() => top))
  if (useMFC) {
    (new ChiselStage).execute(args, generator :+ CIRCTTargetAnnotation(CIRCTTarget.Verilog))
  } else {
    (new chisel3.stage.ChiselStage).execute(args, generator)
  }
}

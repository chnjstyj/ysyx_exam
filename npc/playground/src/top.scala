import chisel3._

/**
  * Compute GCD using subtraction method.
  * Subtracts the smaller from the larger until register y is zero.
  * value in register x is then the GCD
  */
class top extends Module {
  val io = IO(new Bundle {
    val a        = Input(UInt(1.W))
    val b        = Input(UInt(1.W))
    val f     = Output(UInt(1.W))
  })
    io.f := io.a ^ io.b;
}

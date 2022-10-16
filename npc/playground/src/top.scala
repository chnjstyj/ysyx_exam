import chisel3._
import chisel3.util._

/**
  * Compute GCD using subtraction method.
  * Subtracts the smaller from the larger until register y is zero.
  * value in register x is then the GCD
  */
class top extends Module {
  val io = IO(new Bundle {
    val Y        = Input(UInt(2.W))
    val X0        = Input(UInt(2.W))
    val X1        = Input(UInt(2.W))
    val X2        = Input(UInt(2.W))
    val X3        = Input(UInt(2.W))
    val F         = Output(UInt(2.W))
  })

  io.F := 0.U(2.W)
  
  switch (io.Y)
  {
    is (0.U(2.W))
    {
      io.F := io.X0
    }
    is (1.U(2.W))
    {
      io.F := io.X1
    }
    is (2.U(2.W))
    {
      io.F := io.X2
    }
    is (3.U(2.W))
    {
      io.F := io.X3
    }
  }

}

import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.HasBlackBoxInline

class exit_verilator extends BlackBox with HasBlackBoxInline{
  val io = IO(new Bundle{
      val clk = Input(Clock())
      val exit_debugging = Input(UInt(1.W))
  })

  setInline("exit_verilator.v",
  """import "DPI-C" function void exit_ebreak ();
    |module exit_verilator(
    |    input  clk,
    |    input  exit_debugging
    |);
    |always @(negedge clk) begin
    |  if (exit_debugging == 1'b1) begin 
    |     exit_ebreak();
    |  end
    |  else begin
    |  end
    |end
    |endmodule
  """.stripMargin)
}
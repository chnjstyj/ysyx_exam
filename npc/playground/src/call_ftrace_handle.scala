import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.HasBlackBoxInline

class call_ftrace_handle extends BlackBox with HasBlackBoxInline{
  val io = IO(new Bundle{
      val jump_signal = Input(UInt(1.W))
  })

  setInline("call_ftrace_handle.v",
  """import "DPI-C" function void call_ftrace_handle ();
    |module call_ftrace_handle(
    |    input  jump_signal
    |);
    |always @(posedge jump_signal) begin
    |     call_ftrace_handle();
    |end
    |endmodule
  """.stripMargin)
}
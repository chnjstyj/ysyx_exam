import chisel3._
import chisel3.util._

class top extends Module {
  val io = IO(new Bundle {
    val a   = Input(SInt(4.W))
    val b   = Input(SInt(4.W))
    val op  = Input(UInt(3.W))
    val result = Output(SInt(4.W))
  })

  val add_result = WireDefault(io.a + io.b)
  val sub_result = WireDefault(io.a - io.b)
  val not_result = WireDefault(~io.a)
  val and_result = WireDefault(io.a & io.b)
  val or_result = WireDefault(io.a | io.b)
  val xor_result = WireDefault(io.a ^ io.b)
  val less_than_result = WireDefault((io.a < io.b))
  val eq_result = WireDefault((io.a === io.b))

  io.result := 0.S

  switch (io.op) {
    is("b000".U(3.W)){io.result := add_result}
    is("b001".U(3.W)){io.result := sub_result}
    is("b010".U(3.W)){io.result := not_result}
    is("b011".U(3.W)){io.result := and_result}
    is("b100".U(3.W)){io.result := or_result}
    is("b101".U(3.W)){io.result := xor_result}
    is("b110".U(3.W)){io.result := (Cat(0.S(3.W),less_than_result)).asSInt}
    is("b111".U(3.W)){io.result := (Cat(0.S(3.W),eq_result)).asSInt}
  }
  
}

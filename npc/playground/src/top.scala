import chisel3._
import chisel3.util._
import chisel3.experimental._

class top extends Module {
  val io = IO(new Bundle {
    val init = Input(UInt(8.W))
    val sw_clk = Input(Clock())
    val en = Input(UInt(1.W))
    val seg1 = Output(UInt(8.W))
    val seg2 = Output(UInt(8.W))
  })

  val seg_high = Module(new seg)
  val seg_low = Module(new seg)



  withClock(io.sw_clk){
    val shift_reg = RegInit(0.U(8.W))
    val x8 = WireDefault(shift_reg(4) ^ shift_reg(3) ^ shift_reg(2) ^ shift_reg(0))

    when (io.en === 1.U){
      shift_reg := io.init
    }.otherwise{
      shift_reg := Cat(x8,shift_reg(7,1))
    }

    io.seg1 := seg_high.io.output 
    io.seg2 := seg_low.io.output

    seg_high.io.input := shift_reg(7,4)
    seg_low.io.input := shift_reg(3,0)

  }
  
}

import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile
import chisel3.util.experimental.loadMemoryFromFileInline

class top extends Module {
  val io = IO(new Bundle {
    val ps2_clk = Input(UInt(1.W))
    val ps2_data = Input(UInt(1.W))
    val data = Output(UInt(8.W))
    val seg = Output(UInt(16.W))
    val seg_count = Output(UInt(16.W))
  })
  
  val seg1 = Module(new seg)
  val seg2 = Module(new seg)
  val seg3 = Module(new seg)
  val seg4 = Module(new seg)

  val ps2_clk_sync = Reg(UInt(3.W))
  val sampling = WireDefault(ps2_clk_sync(2) & !ps2_clk_sync(1))

  val count = RegInit(0.U(4.W))

  val num_cout = RegInit(0.U(8.W))

  val buffer = Reg(Vec(10, UInt(1.W)))

  val data_reg = RegInit(0.U(8.W))

  val en = RegInit(0.U(1.W))

  val break = RegInit(0.U(1.W))

  val mem = Mem(255,UInt(8.W)) 
  loadMemoryFromFile(mem,"keyboard.hex")

  seg1.io.input := io.data(7,4)
  seg2.io.input := io.data(3,0)
  seg3.io.input := num_cout(7,4)
  seg4.io.input := num_cout(3,0)

  when(en === 1.U){
    io.seg := Cat(seg1.io.output,seg2.io.output)
    io.seg_count := Cat(seg3.io.output,seg4.io.output)
  }.otherwise{
    io.seg := "hffff".U
    io.seg_count := "hffff".U
  }
  
  ps2_clk_sync := Cat(ps2_clk_sync(1,0),io.ps2_clk)

  io.data := data_reg

  when(sampling === 1.U){
    when(count === 10.U){
      when (buffer(0) === 0.U && (io.ps2_data === 1.U) && buffer.asUInt(9,1).xorR){ 
        //finish
        when(buffer.asUInt(8,1) === "hf0".U){
          num_cout := num_cout + 1.U
          break := 1.U
        }.elsewhen(buffer.asUInt(8,1) =/= 0.U){
          //data_reg := convert(buffer.asUInt(8,1))
          data_reg := mem.read(buffer.asUInt(8,1))
          when(break === 1.U){
            en := 0.U
            break := 0.U
          }.otherwise{
            en := 1.U
          }
        }
      }
    count := 0.U
    }.otherwise{    
      buffer(count) := io.ps2_data
      count := count + 1.U
    }
  }.otherwise{
  }
  
  //io.data := fifo(r_ptr + 1.U)

}

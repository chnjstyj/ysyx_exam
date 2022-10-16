import chisel3._
import chisel3.util._

class top extends Module {
  val io = IO(new Bundle {
    val X   = Input(UInt(8.W))
    val en  = Input(UInt(1.W))
    val output_en = Output(UInt(1.W))
    val F   = Output(UInt(3.W))
    val seg = Output(UInt(8.W))
  })

  val result = RegInit(0.U(3.W))

  val numList = (List(0,1,2,3,4,5,6,7)).reverse

  val seg = Module(new seg)

  seg.io.input := result 

  when(io.en === 0.U){
    io.F := 0.U 
    io.seg := 0.U
  }.otherwise{
    io.F := result
    io.seg := seg.io.output
  }

  for (i <- numList){
    when (io.X === 0.U){
      result := 0.U
    }.elsewhen (io.X(i) === 1.U){
      result := i.U(3.W)
    }
  }

  when (io.X === 0.U){
    io.output_en := 0.U
  }.otherwise{
    io.output_en := 1.U
  }

}

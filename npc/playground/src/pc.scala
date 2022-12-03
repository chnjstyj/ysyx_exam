import chisel3._
import chisel3.util._
import chisel3.experimental._

class pc extends Module{
    val io = IO(new Bundle{
        val inst_address = Output(UInt(32.W))
    })

    val inst_address = RegInit("h8000_0000".U(32.W))
    io.inst_address := inst_address 

    
    inst_address := inst_address + 4.U

}
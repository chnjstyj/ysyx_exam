import chisel3._
import chisel3.util._
import chisel3.experimental._

class pc extends Module{
    val io = IO(new Bundle{
        val inst_address = Output(UInt(32.W))
        val ce = Output(UInt(1.W))
    })

    val ce = RegInit(0.U(1.W))
    val inst_address = Reg(UInt(32.W))
    io.inst_address := inst_address 

    ce := 1.U
    when (ce === 0.U){
        inst_address := "h8000_0000".U 
    }.otherwise{
        inst_address := inst_address + 4.U
    }
    io.ce := ce

}
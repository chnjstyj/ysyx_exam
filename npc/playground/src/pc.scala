import chisel3._
import chisel3.util._
import chisel3.experimental._

class pc extends Module{
    val io = IO(new Bundle{
        val inst_address = Output(UInt(64.W))
        val next_inst_address = Output(UInt(64.W))
        val ce = Output(UInt(1.W))

        val direct_jump = Input(UInt(1.W))
        val direct_jump_addr = Input(UInt(64.W))
    })

    val ce = RegInit(0.U(1.W))
    val inst_address = Reg(UInt(64.W))
    io.inst_address := inst_address 
    val next_inst_address = WireDefault(inst_address + 4.U)
    io.next_inst_address := next_inst_address

    ce := 1.U
    when (ce === 0.U){
        inst_address := "h0000_0000_8000_0000".U 
    }.elsewhen (io.direct_jump === 1.U){
        inst_address := io.direct_jump_addr
    }.otherwise{
        inst_address := next_inst_address
    }
    io.ce := ce

}
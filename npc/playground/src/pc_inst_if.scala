import chisel3._
import chisel3.util._

class pc_inst_if extends Module{
    val io = IO(new Bundle{
        val pc_inst_address = Input(UInt(64.W))
        val pc_next_inst_address = Input(UInt(64.W))
        val pc_ce = Input(UInt(1.W))

        val inst_if_inst_address = Output(UInt(64.W))
        val inst_if_next_inst_address = Output(UInt(64.W))
        val inst_if_ce = Output(UInt(1.W)) 

        val stall_pc_inst_if = Input(Bool())
    })

    val enable = WireDefault(!io.stall_pc_inst_if)

    io.inst_if_inst_address := RegEnable(io.pc_inst_address,0.U,enable)
    io.inst_if_next_inst_address := RegEnable(io.pc_next_inst_address,0.U,enable)    
    io.inst_if_ce := RegEnable(io.pc_ce,0.U,enable)
}
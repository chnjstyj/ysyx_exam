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
    })

    io.inst_if_inst_address := RegNext(io.pc_inst_address,0.U)
    io.inst_if_next_inst_address := RegNext(io.pc_next_inst_address,0.U)    
    io.inst_if_ce := RegNext(io.pc_ce,0.U)
}
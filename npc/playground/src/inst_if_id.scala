import chisel3._
import chisel3.util._

class inst_if_id extends Module{
    val io = IO(new Bundle {
        val inst_if_inst = Input(UInt(32.W))
        val inst_if_next_inst_address = Input(UInt(64.W))
        
        val id_inst = Output(UInt(32.W))
        val id_next_inst_address = Output(UInt(64.W))
    })

    io.id_inst := RegNext(io.inst_if_inst, 0.U)
    io.id_next_inst_address := RegNext(io.inst_if_next_inst_address, 0.U)
}
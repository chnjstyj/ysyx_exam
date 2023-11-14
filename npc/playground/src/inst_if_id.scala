import chisel3._
import chisel3.util._

class inst_if_id extends Module{
    val io = IO(new Bundle {
        val inst_if_inst = Input(UInt(32.W))
        val inst_if_inst_address = Input(UInt(64.W))
        val inst_if_next_inst_address = Input(UInt(64.W))
        
        val id_inst = Output(UInt(32.W))
        val id_inst_address = Output(UInt(64.W))
        val id_next_inst_address = Output(UInt(64.W))

        val stall_inst_if_id = Input(Bool())
    })

    val enable = WireDefault(!io.stall_inst_if_id)

    io.id_inst := RegEnable(io.inst_if_inst, 0.U,enable)
    io.id_inst_address := RegEnable(io.inst_if_inst_address,enable)
    io.id_next_inst_address := RegEnable(io.inst_if_next_inst_address,enable)
}
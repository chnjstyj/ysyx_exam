import chisel3._
import chisel3.util._

class inst_if_id extends RawModule{
    val io = IO(new Bundle {
        val clk = Input(Clock())
        val rst = Input(Reset())
        val inst_if_inst = Input(UInt(32.W))
        val inst_if_inst_address = Input(UInt(64.W))
        val inst_if_next_inst_address = Input(UInt(64.W))
        val inst_if_ce = Input(Bool())
        
        val id_inst = Output(UInt(32.W))
        val id_inst_address = Output(UInt(64.W))
        val id_next_inst_address = Output(UInt(64.W))
        val id_ce = Output(Bool())

        val stall_inst_if_id = Input(Bool())
    })

    withClockAndReset(io.clk,io.rst){
        val enable = WireDefault(!io.stall_inst_if_id)

        io.id_inst := RegEnable(io.inst_if_inst, 0.U,enable)
        io.id_inst_address := RegEnable(io.inst_if_inst_address,enable)
        io.id_next_inst_address := RegEnable(io.inst_if_next_inst_address,enable)
        io.id_ce := RegEnable(io.inst_if_ce, false.B,enable)


        val id_ce = RegInit(false.B)
        when (io.stall_inst_if_id){
            io.id_ce := false.B
        }.otherwise{
            io.id_ce := id_ce
        }
        when (io.stall_inst_if_id){
            id_ce := false.B
        }.otherwise{
            id_ce := io.inst_if_ce
        }
    }
}
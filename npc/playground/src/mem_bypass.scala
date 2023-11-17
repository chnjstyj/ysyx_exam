import chisel3._
import chisel3.util._

class mem_bypass extends Module{
    val io = IO(new Bundle{
        val mem_rs2 = Input(UInt(5.W))
        val mem_rs2_rdata = Input(UInt(64.W)) 
        val mem_read_en = Input(Bool()) 

        val wb_rd = Input(UInt(5.W))
        val wb_alu_result = Input(UInt(5.W))

        val ca_rd = Input(UInt(5.W))
        val stall_from_mem_bypass = Output(Bool())
        val rs2_rdata = Output(UInt(64.W))
    })

    when (io.mem_rs2 === io.ca_rd && io.mem_read_en){
        io.rs2_rdata := io.mem_rs2_rdata
        io.stall_from_mem_bypass := true.B
    }.elsewhen (io.mem_rs2 === io.wb_rd && io.mem_rs2 =/= 0.U){
        io.rs2_rdata := io.wb_alu_result
        io.stall_from_mem_bypass := false.B 
    }.otherwise{
        io.rs2_rdata := io.mem_rs2_rdata
        io.stall_from_mem_bypass := false.B 
    }


}
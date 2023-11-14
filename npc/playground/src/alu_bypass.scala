import chisel3._
import chisel3.util._

class alu_bypass extends Module {
    val io = IO(new Bundle{
        val ex_rs1 = Input(UInt(5.W))
        val ex_rs2 = Input(UInt(5.W))
        val ex_rs1_rdata = Input(UInt(64.W))
        val ex_rs2_rdata = Input(UInt(64.W))

        val mem_alu_result = Input(UInt(64.W))
        val mem_rd = Input(UInt(64.W))

        val ca_alu_result = Input(UInt(64.W))
        val ca_rd = Input(UInt(5.W))

        val wb_alu_result = Input(UInt(64.W))
        val wb_rd = Input(UInt(5.W))

        val alu_rs1_rdata = Output(UInt(64.W))
        val alu_rs2_rdata = Output(UInt(64.W))
    })

    when (io.ex_rs1 === io.mem_rd){
        //data from last time alu result 
        io.alu_rs1_rdata := io.mem_alu_result
    }.elsewhen (io.ex_rs1 === io.ca_rd){
        io.alu_rs1_rdata := io.ca_alu_result
    }.elsewhen (io.ex_rs1 === io.wb_rd){
        io.alu_rs1_rdata := io.wb_alu_result
    }.otherwise{
        io.alu_rs1_rdata := io.ex_rs1_rdata
    }


    when (io.ex_rs2 === io.mem_rd){
        //data from last time alu result 
        io.alu_rs2_rdata := io.mem_alu_result
    }.elsewhen (io.ex_rs2 === io.ca_rd){
        io.alu_rs2_rdata := io.ca_alu_result
    }.elsewhen (io.ex_rs2 === io.wb_rd){
        io.alu_rs2_rdata := io.wb_alu_result
    }.otherwise{
        io.alu_rs2_rdata := io.ex_rs2_rdata
    }

}
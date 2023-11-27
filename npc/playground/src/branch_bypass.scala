import chisel3._
import chisel3.util._

class branch_bypass extends Module{
    val io = IO(new Bundle{
        val rs1 = Input(UInt(5.W))
        val rs2 = Input(UInt(5.W))
        val rs1_rdata = Input(UInt(64.W))
        val rs2_rdata = Input(UInt(64.W))
        val judge_branch = Input(Bool())
        val jalr_jump = Input(Bool())

        val ca_rd = Input(UInt(5.W))
        val ca_reg_wen = Input(Bool())
        val ca_alu_result = Input(UInt(64.W))
        val ca_mem_read_en = Input(Bool())
        val ca_mem_read_data = Input(UInt(64.W))
        val ca_save_next_inst_addr = Input(Bool())
        val ca_next_inst_address = Input(UInt(64.W))

        val mem_rd = Input(UInt(5.W))
        val mem_reg_wen = Input(Bool())
        val mem_alu_result = Input(UInt(64.W))
        val mem_mem_read_en = Input(Bool()) //stall needed
        val mem_save_next_inst_addr = Input(Bool())
        val mem_next_inst_address = Input(UInt(64.W))

        val ex_rd = Input(UInt(5.W))
        //stall needed
        val ex_reg_wen = Input(Bool())
        val ex_mem_read_en = Input(Bool())
        val ex_save_next_inst_addr = Input(Bool())
        val ex_next_inst_address = Input(UInt(64.W))

        val branch_rs1_rdata = Output(UInt(64.W))
        val branch_rs2_rdata = Output(UInt(64.W))

        val stall_from_branch_bypass = Output(Bool())
    })

    val stall_from_branch_bypass_r = RegNext(io.stall_from_branch_bypass, false.B)

    when (io.rs1 === 0.U){
        io.branch_rs1_rdata := io.rs1_rdata
    }.elsewhen (io.rs1 === io.ex_rd && (io.ex_reg_wen || io.ex_mem_read_en)){
        //stall 
        io.branch_rs1_rdata := io.rs1_rdata
    }.elsewhen (io.rs1 === io.ex_rd && io.ex_save_next_inst_addr){
        io.branch_rs1_rdata := io.ex_next_inst_address
    }.elsewhen (io.rs1 === io.mem_rd && io.mem_mem_read_en){
        //stall
        io.branch_rs1_rdata := io.rs1_rdata
    }.elsewhen (io.rs1 === io.mem_rd && io.mem_reg_wen){
        io.branch_rs1_rdata := io.mem_alu_result
    }.elsewhen (io.rs1 === io.mem_rd && io.mem_save_next_inst_addr){
        io.branch_rs1_rdata := io.mem_next_inst_address
    }.elsewhen (io.rs1 === io.ca_rd && io.ca_mem_read_en){
        io.branch_rs1_rdata := io.ca_mem_read_data
    }.elsewhen (io.rs1 === io.ca_rd && io.ca_reg_wen){
        io.branch_rs1_rdata := io.ca_alu_result
    }.elsewhen (io.rs1 === io.ca_rd && io.ca_save_next_inst_addr){
        io.branch_rs1_rdata := io.ca_next_inst_address
    }.otherwise{
        io.branch_rs1_rdata := io.rs1_rdata
    }

    when (io.rs2 === 0.U){
        io.branch_rs2_rdata := io.rs2_rdata
    }.elsewhen (io.rs2 === io.ex_rd && (io.ex_reg_wen || io.ex_mem_read_en)){
        //stall 
        io.branch_rs2_rdata := io.rs2_rdata
    }.elsewhen (io.rs2 === io.ex_rd && io.ex_save_next_inst_addr){
        io.branch_rs2_rdata := io.ex_next_inst_address
    }.elsewhen (io.rs2 === io.mem_rd && io.mem_mem_read_en){
        //stall
        io.branch_rs2_rdata := io.rs2_rdata
    }.elsewhen (io.rs2 === io.mem_rd && io.mem_reg_wen){
        io.branch_rs2_rdata := io.mem_alu_result
    }.elsewhen (io.rs2 === io.mem_rd && io.mem_save_next_inst_addr){
        io.branch_rs2_rdata := io.mem_next_inst_address
    }.elsewhen (io.rs2 === io.ca_rd && io.ca_mem_read_en){
        io.branch_rs2_rdata := io.ca_mem_read_data
    }.elsewhen (io.rs2 === io.ca_rd && io.ca_reg_wen){
        io.branch_rs2_rdata := io.ca_alu_result
    }.elsewhen (io.rs2 === io.ca_rd && io.ca_save_next_inst_addr){
        io.branch_rs2_rdata := io.ca_next_inst_address
    }.otherwise{
        io.branch_rs2_rdata := io.rs2_rdata
    }

    when (((io.rs1 === io.ex_rd && (io.ex_reg_wen || io.ex_mem_read_en)) 
        || (io.rs2 === io.ex_rd && (io.ex_reg_wen || io.ex_mem_read_en))
        || (io.rs1 === io.mem_rd && io.mem_mem_read_en)
        || (io.rs2 === io.mem_rd && io.mem_mem_read_en))
        && (io.judge_branch || io.jalr_jump) //&& !stall_from_branch_bypass_r
        ){
            io.stall_from_branch_bypass := true.B
        }.otherwise{
            io.stall_from_branch_bypass := false.B
        }

}
import chisel3._
import chisel3.util._

class alu_bypass extends Module {
    val io = IO(new Bundle{
        val ex_rs1 = Input(UInt(5.W))
        val ex_rs2 = Input(UInt(5.W))
        val ex_rs1_rdata = Input(UInt(64.W))
        val ex_rs2_rdata = Input(UInt(64.W))
        val ex_regfile_output_1 = Input(UInt(1.W))
        //val ex_mem_write_en = Input(UInt(1.W))  //bypass for save , update the saved data

        val mem_alu_result = Input(UInt(64.W))
        val mem_rd = Input(UInt(5.W))
        val mem_reg_wen = Input(Bool())
        val mem_mem_read_en = Input(Bool())
        val mem_save_next_inst_addr = Input(Bool())
        val mem_next_inst_address = Input(UInt(64.W))

        val ca_alu_result = Input(UInt(64.W))
        val ca_rd = Input(UInt(5.W))
        val ca_reg_wen = Input(Bool())
        val ca_mem_read_en = Input(Bool())
        val ca_mem_read_data = Input(UInt(64.W))
        val ca_save_next_inst_addr = Input(Bool())
        val ca_next_inst_address = Input(UInt(64.W))

        val wb_alu_result = Input(UInt(64.W))
        val wb_rd = Input(UInt(5.W))
        val wb_reg_wen = Input(Bool())
        val wb_mem_read_en = Input(Bool())
        val wb_mem_read_data = Input(UInt(64.W))
        val wb_csr_write_to_reg = Input(Bool())
        val wb_csr_rdata = Input(UInt(64.W))

        val alu_rs1_rdata = Output(UInt(64.W))
        val alu_rs2_rdata = Output(UInt(64.W))

        val stall_ex_mem = Input(Bool())
        val stall_from_alu_bypass = Output(Bool())
        val stall_alu = Input(Bool())
        val stall_from_mem = Input(Bool())
    })

    val alu_rs1_rdata_r = RegNext(io.alu_rs1_rdata,0.U)
    val alu_rs1_r = RegNext(io.ex_rs1,0.U)
    val alu_rs2_rdata_r = RegNext(io.alu_rs2_rdata,0.U)
    val alu_rs2_r = RegNext(io.ex_rs2,0.U)
    val stall_ex_mem_r = RegNext(io.stall_ex_mem,0.U)
    val stall_from_alu_bypass_r = RegNext(io.stall_from_alu_bypass & !io.stall_from_mem,0.U)

    when (io.stall_alu){
        io.alu_rs1_rdata := alu_rs1_rdata_r
    }.elsewhen (io.ex_regfile_output_1.asBool || io.ex_rs1 === 0.U){
        io.alu_rs1_rdata := io.ex_rs1_rdata
    }.elsewhen (io.ex_rs1 === io.mem_rd && io.mem_reg_wen){
        //data from last time alu result 
        io.alu_rs1_rdata := io.mem_alu_result
    }.elsewhen (io.ex_rs1 === io.mem_rd && io.mem_save_next_inst_addr){
        io.alu_rs1_rdata := io.mem_next_inst_address
    }.elsewhen (io.ex_rs1 === io.ca_rd && io.ca_reg_wen){
        io.alu_rs1_rdata := io.ca_alu_result
    }.elsewhen (io.ex_rs1 === io.ca_rd && io.ca_mem_read_en){
        io.alu_rs1_rdata := io.ca_mem_read_data
    }.elsewhen (io.ex_rs1 === io.ca_rd && io.ca_save_next_inst_addr){
        io.alu_rs1_rdata := io.ca_next_inst_address
    }.elsewhen (io.ex_rs1 === io.wb_rd && io.wb_reg_wen){
        io.alu_rs1_rdata := io.wb_alu_result
    }.elsewhen (io.ex_rs1 === io.wb_rd && io.wb_mem_read_en){
        io.alu_rs1_rdata := io.wb_mem_read_data
    }.elsewhen (io.ex_rs1 === io.wb_rd && io.wb_csr_write_to_reg){
        io.alu_rs1_rdata := io.wb_csr_rdata
    }.elsewhen ((io.stall_ex_mem || stall_ex_mem_r.asBool) && io.ex_rs1 === alu_rs1_r){
        io.alu_rs1_rdata := alu_rs1_rdata_r
    }.otherwise{
        io.alu_rs1_rdata := io.ex_rs1_rdata
    }


    when (io.stall_alu){
        io.alu_rs2_rdata := alu_rs2_rdata_r
    }.elsewhen (io.ex_rs2 === 0.U){
        io.alu_rs2_rdata := io.ex_rs2_rdata
    }.elsewhen (io.ex_rs2 === io.mem_rd && io.mem_reg_wen){
        //data from last time alu result 
        io.alu_rs2_rdata := io.mem_alu_result
    }.elsewhen (io.ex_rs1 === io.mem_rd && io.mem_save_next_inst_addr){
        io.alu_rs2_rdata := io.mem_next_inst_address
    }.elsewhen (io.ex_rs2 === io.ca_rd && io.ca_reg_wen){
        io.alu_rs2_rdata := io.ca_alu_result
    }.elsewhen (io.ex_rs2 === io.ca_rd && io.ca_mem_read_en){
        io.alu_rs2_rdata := io.ca_mem_read_data
    }.elsewhen (io.ex_rs2 === io.ca_rd && io.ca_save_next_inst_addr){
        io.alu_rs2_rdata := io.ca_next_inst_address
    }.elsewhen (io.ex_rs2 === io.wb_rd && io.wb_reg_wen){
        io.alu_rs2_rdata := io.wb_alu_result
    }.elsewhen (io.ex_rs2 === io.wb_rd && io.wb_mem_read_en){
        io.alu_rs2_rdata := io.wb_mem_read_data
    }.elsewhen (io.ex_rs2 === io.wb_rd && io.wb_csr_write_to_reg){
        io.alu_rs2_rdata := io.wb_csr_rdata
    }.elsewhen ((io.stall_ex_mem || stall_ex_mem_r.asBool) && io.ex_rs2 === alu_rs2_r){
        io.alu_rs2_rdata := alu_rs2_rdata_r
    }.otherwise{
        io.alu_rs2_rdata := io.ex_rs2_rdata
    }

    when ((((io.ex_rs1 === io.mem_rd && io.mem_mem_read_en) 
        || (io.ex_rs2 === io.mem_rd && io.mem_mem_read_en)) & !stall_from_alu_bypass_r)
        ){
            io.stall_from_alu_bypass := true.B
        }.otherwise{
            io.stall_from_alu_bypass := false.B
        }

}
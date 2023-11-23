import chisel3._
import chisel3.util._

class ca_wb extends Module{
    val io = IO(new Bundle{
        val ca_alu_result = Input(UInt(64.W))
        val ca_reg_wen = Input(UInt(1.W))
        val ca_rd = Input(UInt(5.W))
        val ca_csr_sen = Input(UInt(1.W))
        val ca_csr_addr = Input(UInt(12.W))
        val ca_mem_read_data = Input(UInt(64.W))
        val ca_mem_read_en = Input(UInt(1.W))
        val ca_exit_debugging = Input(UInt(1.W))
        val ca_ce = Input(Bool())
        val ca_save_next_inst_addr = Input(UInt(1.W))
        val ca_next_inst_address = Input(UInt(64.W))
        val ca_inst = Input(UInt(32.W))

        val wb_alu_result = Output(UInt(64.W))
        val wb_reg_wen = Output(UInt(1.W))
        val wb_rd = Output(UInt(5.W))
        val wb_csr_sen = Output(UInt(1.W))
        val wb_csr_addr = Output(UInt(12.W))
        val wb_mem_read_data = Output(UInt(64.W))
        val wb_mem_read_en = Output(UInt(1.W))
        val wb_exit_debugging = Output(UInt(1.W))
        val wb_save_next_inst_addr = Output(UInt(1.W))
        val wb_next_inst_address = Output(UInt(64.W))
        val wb_inst = Output(UInt(32.W))

        val wb_diff_run = Output(UInt(1.W))

        val stall_ca_wb = Input(Bool())
    })

    val enable = WireDefault(!io.stall_ca_wb)

    io.wb_alu_result := RegEnable(io.ca_alu_result,0.U,enable)
    io.wb_reg_wen := RegEnable(io.ca_reg_wen,0.U,enable)
    io.wb_rd := RegEnable(io.ca_rd,0.U,enable)
    io.wb_csr_sen := RegEnable(io.ca_csr_sen,0.U,enable)
    io.wb_csr_addr := RegEnable(io.ca_csr_addr,0.U,enable)
    io.wb_mem_read_data := RegEnable(io.ca_mem_read_data,0.U,enable)
    io.wb_mem_read_en := RegEnable(io.ca_mem_read_en,0.U,enable)
    io.wb_exit_debugging := RegEnable(io.ca_exit_debugging,0.U,enable)
    io.wb_diff_run := RegEnable(io.ca_ce,false.B,enable)
    io.wb_save_next_inst_addr := RegEnable(io.ca_save_next_inst_addr,0.U,enable)
    io.wb_next_inst_address := RegEnable(io.ca_next_inst_address,0.U,enable)
    io.wb_inst := RegEnable(io.ca_inst,0.U,enable)

}
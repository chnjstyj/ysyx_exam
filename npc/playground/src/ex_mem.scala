import chisel3._
import chisel3.util._


class ex_mem extends Module{
    val io = IO(new Bundle{
        val ex_alu_result = Input(UInt(64.W))
        val ex_reg_wen = Input(UInt(1.W))
        val ex_rd = Input(UInt(5.W))
        val ex_mem_write_en = Input(UInt(1.W))
        val ex_mem_write_mask = Input(UInt(4.W))
        val ex_mem_read_en = Input(UInt(1.W))
        val ex_mem_read_size = Input(UInt(4.W))
        val ex_zero_extends = Input(UInt(1.W))
        val ex_rs2_rdata = Input(UInt(64.W))
        val ex_csr_sen = Input(UInt(1.W))
        val ex_csr_addr = Input(UInt(12.W))
        
        val mem_alu_result = Output(UInt(64.W))
        val mem_reg_wen = Output(UInt(1.W))
        val mem_rd = Output(UInt(5.W))
        val mem_mem_write_en = Output(UInt(1.W))
        val mem_mem_write_mask = Output(UInt(4.W))
        val mem_mem_read_en = Output(UInt(1.W))
        val mem_mem_read_size = Output(UInt(4.W))
        val mem_zero_extends = Output(UInt(1.W))
        val mem_rs2_rdata = Output(UInt(64.W))
        val mem_csr_sen = Output(UInt(1.W))
        val mem_csr_addr = Output(UInt(12.W)) 
    })
    
    io.mem_alu_result := RegNext(io.ex_alu_result,0.U)
    io.mem_reg_wen := RegNext(io.ex_reg_wen,0.U)
    io.mem_rd := RegNext(io.ex_rd,0.U)
    io.mem_mem_write_en := RegNext(io.ex_mem_write_en,0.U)
    io.mem_mem_write_mask := RegNext(io.ex_mem_write_mask,0.U)
    io.mem_mem_read_en := RegNext(io.ex_mem_read_en,0.U)
    io.mem_mem_read_size := RegNext(io.ex_mem_read_size,0.U)
    io.mem_zero_extends := RegNext(io.ex_zero_extends,0.U)
    io.mem_rs2_rdata := RegNext(io.ex_rs2_rdata,0.U)
    io.mem_csr_sen := RegNext(io.ex_csr_sen,0.U)
    io.mem_csr_addr := RegNext(io.ex_csr_addr,0.U)

}
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

        val wb_alu_result = Output(UInt(64.W))
        val wb_reg_wen = Output(UInt(1.W))
        val wb_rd = Output(UInt(5.W))
        val wb_csr_sen = Output(UInt(1.W))
        val wb_csr_addr = Output(UInt(12.W))
        val wb_mem_read_data = Output(UInt(64.W))
        val wb_mem_read_en = Output(UInt(1.W))
    })

    io.wb_alu_result := RegNext(io.ca_alu_result,0.U)
    io.wb_reg_wen := RegNext(io.ca_reg_wen,0.U)
    io.wb_rd := RegNext(io.ca_rd,0.U)
    io.wb_csr_sen := RegNext(io.ca_csr_sen,0.U)
    io.wb_csr_addr := RegNext(io.ca_csr_addr,0.U)
    io.wb_mem_read_data := RegNext(io.ca_mem_read_data,0.U)
    io.wb_mem_read_en := RegNext(io.ca_mem_read_en,0.U)

}
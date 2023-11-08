import chisel3._
import chisel3.util._

class mem_ca extends Module{
    val io = IO(new Bundle{
        val mem_alu_result = Input(UInt(64.W))
        val mem_reg_wen = Input(UInt(1.W))
        val mem_rd = Input(UInt(5.W))
        val mem_csr_sen = Input(UInt(1.W))
        val mem_csr_addr = Input(UInt(12.W))
        val mem_mem_read_en = Input(UInt(1.W))

        val ca_alu_result = Output(UInt(64.W))
        val ca_reg_wen = Output(UInt(1.W))
        val ca_rd = Output(UInt(5.W))
        val ca_csr_sen = Output(UInt(1.W))
        val ca_csr_addr = Output(UInt(12.W))
        val ca_mem_read_en = Output(UInt(1.W))
    })

    io.ca_alu_result := RegNext(io.mem_alu_result,0.U)
    io.ca_reg_wen := RegNext(io.mem_reg_wen,0.U)
    io.ca_rd := RegNext(io.mem_rd,0.U)
    io.ca_csr_sen := RegNext(io.mem_csr_sen,0.U)
    io.ca_csr_addr := RegNext(io.mem_csr_addr,0.U)
    io.ca_mem_read_en := RegNext(io.mem_mem_read_en,0.U)

}
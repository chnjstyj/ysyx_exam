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
        val mem_exit_debugging = Input(UInt(1.W))

        val ca_alu_result = Output(UInt(64.W))
        val ca_reg_wen = Output(UInt(1.W))
        val ca_rd = Output(UInt(5.W))
        val ca_csr_sen = Output(UInt(1.W))
        val ca_csr_addr = Output(UInt(12.W))
        val ca_mem_read_en = Output(UInt(1.W))
        val ca_exit_debugging = Output(UInt(1.W))

        val stall_mem_ca = Input(Bool())
    })

    val enable = WireDefault(!io.stall_mem_ca)

    io.ca_alu_result := RegEnable(io.mem_alu_result,0.U,enable)
    io.ca_reg_wen := RegEnable(io.mem_reg_wen,0.U,enable)
    io.ca_rd := RegEnable(io.mem_rd,0.U,enable)
    io.ca_csr_sen := RegEnable(io.mem_csr_sen,0.U,enable)
    io.ca_csr_addr := RegEnable(io.mem_csr_addr,0.U,enable)
    io.ca_mem_read_en := RegEnable(io.mem_mem_read_en,0.U,enable)
    io.ca_exit_debugging := RegEnable(io.mem_exit_debugging,0.U,enable)

}
import chisel3._
import chisel3.util._

class id_ex(
    alu_control_width:Int
) extends Module{
    val io = IO(new Bundle{
        val id_alu_src = Input(UInt(1.W))
        val id_alu_control = Input(UInt(alu_control_width.W))
        val id_reg_wen = Input(UInt(1.W))
        //val id_direct_jump
        //val save_next_inst_addr
        val id_mem_write_en = Input(UInt(1.W))
        val id_mem_write_wmask = Input(UInt(4.W))
        val id_mem_read_en = Input(UInt(1.W))
        val id_mem_read_size = Input(UInt(4.W))
        val id_alu_result_size = Input(UInt(1.W))
        val id_zero_extends = Input(UInt(1.W))
        val id_funct3 = Input(UInt(3.W))
        val id_rs1_rdata = Input(UInt(64.W))
        val id_rs2_rdata = Input(UInt(64.W))
        val id_csr_rdata = Input(UInt(64.W))
        val id_imm = Input(UInt(64.W))
        val id_sign_less_than = Input(UInt(1.W))
        val id_csr_sen = Input(UInt(1.W))

        val ex_alu_src = Output(UInt(1.W))
        val ex_alu_control = Output(UInt(alu_control_width.W))
        val ex_reg_wen = Output(UInt(1.W))
        val ex_mem_write_en = Output(UInt(1.W))
        val ex_mem_write_mask = Output(UInt(4.W))
        val ex_mem_read_en = Output(UInt(1.W))
        val ex_mem_read_size = Output(UInt(4.W))
        val ex_alu_result_size = Output(UInt(1.W))
        val ex_zero_extends = Output(UInt(1.W))
        val ex_funct3 = Output(UInt(3.W))
        val ex_rs1_rdata = Output(UInt(64.W))
        val ex_rs2_rdata = Output(UInt(64.W))
        val ex_csr_rdata = Output(UInt(64.W))
        val ex_imm = Output(UInt(64.W))
        val ex_sign_less_than = Output(UInt(1.W))
        val ex_csr_sen = Output(UInt(1.W))
    })

    io.ex_alu_src := RegNext(io.id_alu_src,0.U)
    io.ex_alu_control := RegNext(io.id_alu_control,0.U)
    io.ex_reg_wen := RegNext(io.id_reg_wen,0.U)
    io.ex_mem_write_en := RegNext(io.id_mem_write_en,0.U)
    io.ex_mem_write_mask := RegNext(io.id_mem_write_wmask,0.U)
    io.ex_mem_read_en := RegNext(io.id_mem_read_en,0.U)
    io.ex_mem_read_size := RegNext(io.id_mem_read_size,0.U)
    io.ex_alu_result_size := RegNext(io.id_alu_result_size,0.U)
    io.ex_zero_extends := RegNext(io.id_zero_extends,0.U)
    io.ex_funct3 := RegNext(io.id_funct3,0.U)
    io.ex_rs1_rdata := RegNext(io.id_rs1_rdata,0.U)
    io.ex_rs2_rdata := RegNext(io.id_rs2_rdata,0.U)
    io.ex_csr_rdata := RegNext(io.id_csr_rdata,0.U)
    io.ex_imm := RegNext(io.id_imm,0.U)
    io.ex_sign_less_than := RegNext(io.id_sign_less_than,0.U)
    io.ex_csr_sen := RegNext(io.id_csr_sen,0.U)

}
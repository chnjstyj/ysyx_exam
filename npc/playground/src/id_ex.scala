import chisel3._
import chisel3.util._

class id_ex(
    alu_control_width:Int
) extends RawModule{
    val io = IO(new Bundle{
        val clk = Input(Clock())
        val rst = Input(Reset())
        val id_alu_src = Input(UInt(1.W))
        val id_alu_control = Input(UInt(alu_control_width.W))
        val id_reg_wen = Input(UInt(1.W))
        val id_rd = Input(UInt(5.W))
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
        val id_csr_addr = Input(UInt(12.W))
        val id_sign_divrem = Input(UInt(1.W))
        val id_rs1 = Input(UInt(5.W))
        val id_rs2 = Input(UInt(5.W))
        val id_exit_debugging = Input(UInt(1.W))
        val id_ce = Input(Bool())
        val id_save_next_inst_addr = Input(UInt(1.W))
        val id_next_inst_address = Input(UInt(64.W))
        val id_inst_address = Input(UInt(64.W))
        val id_regfile_output_1 = Input(UInt(1.W))
        val id_inst = Input(UInt(32.W))

        val ex_alu_src = Output(UInt(1.W))
        val ex_alu_control = Output(UInt(alu_control_width.W))
        val ex_reg_wen = Output(UInt(1.W))
        val ex_rd = Output(UInt(5.W))
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
        val ex_csr_addr = Output(UInt(12.W))
        val ex_sign_divrem = Output(UInt(1.W))
        val ex_rs1 = Output(UInt(5.W))
        val ex_rs2 = Output(UInt(5.W))
        val ex_exit_debugging = Output(UInt(1.W))
        val ex_ce = Output(Bool())
        val ex_save_next_inst_addr = Output(Bool())
        val ex_next_inst_address = Output(UInt(64.W))
        val ex_inst_address = Output(UInt(64.W))
        val ex_regfile_output_1 = Output(UInt(1.W))
        val ex_inst = Output(UInt(32.W))

        val stall_id_ex = Input(Bool())
    })

    withClockAndReset(io.clk,io.rst)
    {
        val enable = WireDefault(!io.stall_id_ex)

        io.ex_alu_src := RegEnable(io.id_alu_src,0.U,enable)
        io.ex_alu_control := RegEnable(io.id_alu_control,0.U,enable)
        io.ex_reg_wen := RegEnable(io.id_reg_wen,0.U,enable)
        io.ex_rd := RegEnable(io.id_rd,0.U,enable)
        io.ex_mem_write_en := RegEnable(io.id_mem_write_en,0.U,enable)
        io.ex_mem_write_mask := RegEnable(io.id_mem_write_wmask,0.U,enable)
        io.ex_mem_read_en := RegEnable(io.id_mem_read_en,0.U,enable)
        io.ex_mem_read_size := RegEnable(io.id_mem_read_size,0.U,enable)
        io.ex_alu_result_size := RegEnable(io.id_alu_result_size,0.U,enable)
        io.ex_zero_extends := RegEnable(io.id_zero_extends,0.U,enable)
        io.ex_funct3 := RegEnable(io.id_funct3,0.U,enable)
        io.ex_rs1_rdata := RegEnable(io.id_rs1_rdata,0.U,enable)
        io.ex_rs2_rdata := RegEnable(io.id_rs2_rdata,0.U,enable)
        io.ex_csr_rdata := RegEnable(io.id_csr_rdata,0.U,enable)
        io.ex_imm := RegEnable(io.id_imm,0.U,enable)
        io.ex_sign_less_than := RegEnable(io.id_sign_less_than,0.U,enable)
        io.ex_csr_sen := RegEnable(io.id_csr_sen,0.U,enable)
        io.ex_csr_addr := RegEnable(io.id_csr_addr,0.U,enable)
        io.ex_sign_divrem := RegEnable(io.id_sign_divrem,0.U,enable)
        io.ex_rs1 := RegEnable(io.id_rs1,0.U,enable)
        io.ex_rs2 := RegEnable(io.id_rs2,0.U,enable)
        io.ex_exit_debugging := RegEnable(io.id_exit_debugging,0.U,enable)
        //io.ex_ce := RegEnable(io.id_ce,false.B,enable)
        io.ex_save_next_inst_addr := RegEnable(io.id_save_next_inst_addr,false.B,enable)
        io.ex_next_inst_address := RegEnable(io.id_next_inst_address,0.U,enable)
        io.ex_regfile_output_1 := RegEnable(io.id_regfile_output_1,0.U,enable)
        io.ex_ce := RegEnable(io.id_ce,false.B,enable)
        io.ex_inst := RegEnable(io.id_inst,0.U,enable)
        io.ex_inst_address := RegEnable(io.id_inst_address,0.U,enable)
    }

    /*
    val ex_ce = RegInit(false.B)
    //when (io.stall_id_ex){
        //io.ex_ce := false.B
    //}.otherwise{
        io.ex_ce := ex_ce
    //}
    when (io.stall_id_ex){
        ex_ce := RegNext(false.B)
    }.otherwise{
        ex_ce := io.id_ce
    }*/

}
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
        val ex_rs2 = Input(UInt(5.W))
        val ex_rs2_rdata = Input(UInt(64.W))
        val ex_csr_sen = Input(UInt(1.W))
        val ex_csr_addr = Input(UInt(12.W))
        val ex_exit_debugging = Input(UInt(1.W))
        val ex_ce = Input(Bool())
        val ex_save_next_inst_addr = Input(UInt(1.W))
        val ex_next_inst_address = Input(UInt(64.W))
        val ex_inst = Input(UInt(32.W))
        
        val mem_alu_result = Output(UInt(64.W))
        val mem_reg_wen = Output(UInt(1.W))
        val mem_rd = Output(UInt(5.W))
        val mem_mem_write_en = Output(UInt(1.W))
        val mem_mem_write_mask = Output(UInt(4.W))
        val mem_mem_read_en = Output(UInt(1.W))
        val mem_mem_read_size = Output(UInt(4.W))
        val mem_zero_extends = Output(UInt(1.W))
        val mem_rs2 = Output(UInt(5.W))
        val mem_rs2_rdata = Output(UInt(64.W))
        val mem_csr_sen = Output(UInt(1.W))
        val mem_csr_addr = Output(UInt(12.W)) 
        val mem_exit_debugging = Output(UInt(1.W))
        val mem_ce = Output(Bool())
        val mem_save_next_inst_addr = Output(UInt(1.W))
        val mem_next_inst_address = Output(UInt(64.W))
        val mem_inst = Output(UInt(32.W))

        val stall_ex_mem = Input(Bool())
    })

    val enable = WireDefault(!io.stall_ex_mem)
    
    io.mem_alu_result := RegEnable(io.ex_alu_result,0.U,enable)
    io.mem_reg_wen := RegEnable(io.ex_reg_wen,0.U,enable)
    io.mem_rd := RegEnable(io.ex_rd,0.U,enable)
    io.mem_mem_write_en := RegEnable(io.ex_mem_write_en,0.U,enable)
    io.mem_mem_write_mask := RegEnable(io.ex_mem_write_mask,0.U,enable)
    io.mem_mem_read_en := RegEnable(io.ex_mem_read_en,0.U,enable)
    io.mem_mem_read_size := RegEnable(io.ex_mem_read_size,0.U,enable)
    io.mem_zero_extends := RegEnable(io.ex_zero_extends,0.U,enable)
    io.mem_rs2 := RegEnable(io.ex_rs2,0.U,enable) 
    io.mem_rs2_rdata := RegEnable(io.ex_rs2_rdata,0.U,enable)
    io.mem_csr_sen := RegEnable(io.ex_csr_sen,0.U,enable)
    io.mem_csr_addr := RegEnable(io.ex_csr_addr,0.U,enable)
    io.mem_exit_debugging := RegEnable(io.ex_exit_debugging,0.U,enable)
    //io.mem_ce := RegEnable(io.ex_ce,false.B,enable)
    io.mem_save_next_inst_addr := RegEnable(io.ex_save_next_inst_addr,0.U,enable)
    io.mem_next_inst_address := RegEnable(io.ex_next_inst_address,0.U,enable)
    io.mem_ce := RegEnable(io.ex_ce,false.B,enable)
    io.mem_inst := RegEnable(io.ex_inst,0.U,enable)

    /*
    val mem_ce = RegInit(false.B)
    //when (io.stall_ex_mem){
    //    io.mem_ce := false.B
    //}.otherwise{
        io.mem_ce := mem_ce
    //}
    when (io.stall_ex_mem){
        mem_ce := RegNext(false.B)
    }.otherwise{
        mem_ce := io.ex_ce
    }*/
}
import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile
import chisel3.util.experimental.loadMemoryFromFileInline

class top extends Module{
    val io = IO(new Bundle{
        val inst = Output(UInt(32.W))
        val inst_address = Output(UInt(64.W))
        val next_inst_address = Output(UInt(64.W))
        val stall = Output(Bool())
    })

    val alu_control_width = 4

    val pc = Module(new pc) 
    val axi_lite_arbiter = Module(new axi_lite_arbiter)
    val inst_if = Module(new inst_if("inst.rom")) 
    val id = Module(new id(alu_control_width))
    val regfile = Module(new regfile)
    val alu = Module(new alu(alu_control_width))
    val stall = Module(new stall)
    val mem = Module(new mem)
    val judge_branch_m = Module(new judge_branch_m)

    io.inst := inst_if.io.inst
    when (pc.io.direct_jump === 1.U){
        io.inst_address := pc.io.inst_address | "h8000_0000".U 
    }.otherwise{
        io.inst_address := pc.io.inst_address | "h8000_0000".U  
    }
    io.next_inst_address := pc.io.next_inst_address
    io.stall := RegNext( stall.io.stall_global)//inst_if.io.stall_from_inst_if | mem.io.stall_from_mem

    withClock((!clock.asBool).asClock){
        val direct_jump_r = RegNext( id.io.control_signal.direct_jump )
        val direct_jump_addr_r = RegNext( alu.io.alu_result )
        pc.io.direct_jump := direct_jump_r
        pc.io.direct_jump_addr := direct_jump_addr_r
    }

    pc.io.branch_jump := judge_branch_m.io.branch_jump
    pc.io.branch_jump_addr := judge_branch_m.io.branch_jump_addr
    pc.io.ecall := id.io.control_signal.ecall
    pc.io.ecall_addr := regfile.io.csr_rdata
    pc.io.mret := id.io.control_signal.mret
    pc.io.mret_addr := regfile.io.mret_addr
    pc.io.stall_global := stall.io.stall_global

    inst_if.io.ACLK := clock
    inst_if.io.ARESETn := ~(reset.asBool)
    inst_if.io.inst_address := pc.io.inst_address
    inst_if.io.ce := pc.io.ce
    inst_if.io.stall_global := stall.io.stall_global
    inst_if.io.stall_from_mem_reg := RegNext(stall.io.stall_from_mem_reg)
    inst_if.io.ifu_read_data := axi_lite_arbiter.io.ifu_read_data
    inst_if.io.ifu_read_valid := axi_lite_arbiter.io.ifu_read_valid

    id.io.inst := inst_if.io.inst

    judge_branch_m.io.judge_branch := id.io.control_signal.judge_branch
    judge_branch_m.io.imm := id.io.imm 
    judge_branch_m.io.rs1_rdata := regfile.io.rs1_rdata
    judge_branch_m.io.rs2_rdata := regfile.io.rs2_rdata
    judge_branch_m.io.inst_address := pc.io.inst_address
    judge_branch_m.io.funct3 := id.io.funct3

    regfile.io.rs1 := id.io.rs1 
    regfile.io.rs2 := id.io.rs2 
    regfile.io.rd := id.io.rd 
    regfile.io.reg_wen := id.io.control_signal.reg_wen
    regfile.io.regfile_output_1 := id.io.control_signal.regfile_output_1
    regfile.io.inst_address := pc.io.inst_address
    regfile.io.save_next_inst_addr := id.io.control_signal.save_next_inst_addr
    regfile.io.mem_read_en := id.io.control_signal.mem_read_en
    regfile.io.rd_wdata := MuxCase(alu.io.alu_result,Seq(
        id.io.control_signal.save_next_inst_addr.asBool -> pc.io.next_inst_address,
        id.io.control_signal.mem_read_en.asBool -> mem.io.mem_read_data))
    regfile.io.csr_wen := id.io.control_signal.csr_wen
    regfile.io.csr_sen := id.io.control_signal.csr_sen
    regfile.io.csr_addr := id.io.imm
    regfile.io.ecall := id.io.control_signal.ecall
    regfile.io.csr_write_to_reg := id.io.control_signal.csr_write_to_reg
    /*
    when (id.io.control_signal.save_next_inst_addr === 1.U){
        regfile.io.rd_wdata := pc.io.next_inst_address
    }.elsewhen (id.io.control_signal.mem_read_en === 1.U){
        regfile.io.rd_wdata := mem.io.mem_read_data
    }.otherwise{
        regfile.io.rd_wdata := alu.io.alu_result
    }*/

    alu.io.alu_control := id.io.control_signal.alu_control
    alu.io.alu_src := id.io.control_signal.alu_src
    alu.io.rs1_rdata := regfile.io.rs1_rdata
    alu.io.rs2_rdata := regfile.io.rs2_rdata
    alu.io.imm := id.io.imm
    alu.io.alu_result_size := id.io.control_signal.alu_result_size
    alu.io.sign_less_than := id.io.control_signal.sign_less_than
    alu.io.sign_divrem := id.io.control_signal.sign_divrem
    alu.io.funct3 := id.io.funct3 
    alu.io.csr_sen := id.io.control_signal.csr_sen
    alu.io.csr_rdata := regfile.io.csr_rdata
    
    mem.io.ACLK := clock
    mem.io.ARESETn := ~(reset.asBool)
    mem.io.mem_addr := alu.io.alu_result
    mem.io.mem_write_data := regfile.io.rs2_rdata
    mem.io.mem_write_en := id.io.control_signal.mem_write_en
    mem.io.mem_wmask := id.io.control_signal.mem_wmask
    mem.io.mem_read_en := id.io.control_signal.mem_read_en
    mem.io.mem_read_size := id.io.control_signal.mem_read_size
    mem.io.zero_extends := id.io.control_signal.zero_extends
    mem.io.mem_read_valid := axi_lite_arbiter.io.lsu_read_valid
    mem.io.mem_rdata := axi_lite_arbiter.io.lsu_read_data
    mem.io.mem_write_finish := axi_lite_arbiter.io.lsu_write_finish


    stall.io.exit_debugging := id.io.control_signal.exit_debugging
    stall.io.stall_from_inst_if := inst_if.io.stall_from_inst_if
    stall.io.stall_from_mem := mem.io.stall_from_mem

    axi_lite_arbiter.io.ACLK := clock 
    axi_lite_arbiter.io.ARESETn := ~(reset.asBool)
    axi_lite_arbiter.io.ifu_read_addr := inst_if.io.ifu_read_addr
    axi_lite_arbiter.io.ifu_read_en := inst_if.io.ifu_read_en
    axi_lite_arbiter.io.lsu_addr := mem.io.lsu_addr
    axi_lite_arbiter.io.lsu_read_en := mem.io.lsu_read_en
    axi_lite_arbiter.io.lsu_write_data := mem.io.lsu_write_data
    axi_lite_arbiter.io.lsu_write_en := mem.io.lsu_write_en
    axi_lite_arbiter.io.lsu_write_mask := mem.io.lsu_write_mask

}
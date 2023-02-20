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
    })

    val alu_control_width = 4

    val pc = Module(new pc) 
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

    pc.io.direct_jump := id.io.control_signal.direct_jump
    pc.io.direct_jump_addr := alu.io.alu_result
    pc.io.branch_jump := judge_branch_m.io.branch_jump
    pc.io.branch_jump_addr := judge_branch_m.io.branch_jump_addr

    inst_if.io.clock := clock
    inst_if.io.inst_address := pc.io.inst_address
    inst_if.io.ce := pc.io.ce

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
    regfile.io.rd_wdata := alu.io.alu_result
    regfile.io.save_next_inst_addr := id.io.control_signal.save_next_inst_addr
    regfile.io.next_inst_address := pc.io.next_inst_address

    alu.io.alu_control := id.io.control_signal.alu_control
    alu.io.alu_src := id.io.control_signal.alu_src
    alu.io.rs1_rdata := regfile.io.rs1_rdata
    alu.io.rs2_rdata := regfile.io.rs2_rdata
    alu.io.imm := id.io.imm

    stall.io.exit_debugging := id.io.control_signal.exit_debugging
    
    mem.io.clock := clock
    mem.io.mem_write_addr := alu.io.alu_result
    mem.io.mem_write_data := regfile.io.rs2_rdata
    mem.io.mem_write_en := id.io.control_signal.mem_write_en
    mem.io.mem_wmask := id.io.control_signal.mem_wmask

}
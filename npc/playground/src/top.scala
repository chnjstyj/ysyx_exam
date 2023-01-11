import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile
import chisel3.util.experimental.loadMemoryFromFileInline

class top extends Module{
    val io = IO(new Bundle{
        val inst = Output(UInt(32.W))
        val inst_address = Output(UInt(64.W))
    })

    val alu_control_width = 4

    val pc = Module(new pc) 
    val inst_if = Module(new inst_if("inst.rom")) 
    val id = Module(new id(alu_control_width))
    val regfile = Module(new regfile)
    val alu = Module(new alu(alu_control_width))
    val stall = Module(new stall)

    io.inst := inst_if.io.inst
    io.inst_address := pc.io.inst_address

    pc.io.direct_jump := id.io.control_signal.direct_jump
    pc.io.direct_jump_addr := alu.io.alu_result

    inst_if.io.clock := clock
    inst_if.io.inst_address := pc.io.inst_address
    inst_if.io.ce := pc.io.ce

    id.io.inst := inst_if.io.inst

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
    

}
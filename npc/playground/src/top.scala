import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile
import chisel3.util.experimental.loadMemoryFromFileInline

class top extends Module{
    val io = IO(new Bundle{
        val inst = Output(UInt(32.W))
        val imm = Output(UInt(64.W))
        val alu_result = Output(UInt(64.W))
    })

    val alu_control_width = 4

    val pc = Module(new pc) 
    val inst_if = Module(new inst_if) 
    val id = Module(new id(alu_control_width))
    val regfile = Module(new regfile)
    val alu = Module(new alu(alu_control_width))

    io.inst := inst_if.io.inst
    io.imm := id.io.imm
    io.alu_result := alu.io.alu_result

    inst_if.io.inst_address := pc.io.inst_address
    inst_if.io.ce := pc.io.ce

    id.io.inst := inst_if.io.inst

    regfile.io.rs1 := id.io.rs1 
    regfile.io.rs2 := id.io.rs2 
    regfile.io.rd := id.io.rd 
    regfile.io.reg_wen := id.io.control_signal.reg_wen
    regfile.io.rd_wdata := alu.io.alu_result

    alu.io.alu_control := id.io.control_signal.alu_control
    alu.io.alu_src := id.io.control_signal.alu_src
    alu.io.rs1_rdata := regfile.io.rs1_rdata
    alu.io.rs2_rdata := regfile.io.rs2_rdata
    alu.io.imm := id.io.imm
    

}
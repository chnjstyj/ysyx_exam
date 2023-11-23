import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.HasBlackBoxInline
import java.io.File


class stall extends Module{
    val io = IO(new Bundle{
        val exit_debugging = Input(UInt(1.W))
        val stall_from_inst_if = Input(UInt(1.W))
        val stall_from_mem = Input(UInt(1.W))
        val stall_from_alu = Input(Bool())
        val stall_from_alu_bypass = Input(Bool())
        val stall_from_mem_bypass = Input(Bool())
        val stall_from_mem_reg = Output(UInt(1.W))
        val stall_from_ca = Input(Bool())
        val stall_from_dcache = Input(UInt(1.W))
        val stall_from_branch_bypass = Input(Bool())
        val branch_jump = Input(Bool())
        val direct_jump = Input(Bool())

        val stall_global = Output(UInt(1.W))
        //val stall_pc_inst_if = Output(UInt(1.W))
        val stall_pc = Output(UInt(1.W))
        val stall_inst_if_id = Output(UInt(1.W))
        val stall_id_ex = Output(UInt(1.W))
        val stall_ex_mem = Output(UInt(1.W))
        val stall_mem_ca = Output(UInt(1.W))
        val stall_ca_wb = Output(UInt(1.W))

        //val flush_pc_inst_if = Output(Bool())
        val flush_inst_if_id = Output(Bool())
        val flush_id_ex = Output(Bool())
        val flush_ex_mem = Output(Bool())
        val flush_mem_ca = Output(Bool())
        val flush_ca_wb = Output(Bool())

        val icache_miss = Input(Bool())
    })

    val exit_verilator = Module(new exit_verilator)

    exit_verilator.io.clk := clock
    exit_verilator.io.exit_debugging := RegNext(io.exit_debugging)

    
    withClock (clock){
        val stall_mem_reg = RegNext(io.stall_from_mem)
        val stall_inst_if_reg = RegNext(io.stall_from_inst_if)
        val stall_alu_reg = RegNext(io.stall_from_alu)
        io.stall_global := io.stall_from_mem | io.stall_from_inst_if | stall_alu_reg

        //io.stall_pc_inst_if := stall_inst_if_reg | stall_alu_reg | io.stall_from_mem | io.stall_from_mem_bypass
        io.stall_pc := io.stall_from_inst_if | io.stall_from_alu | io.stall_from_mem | io.stall_from_mem_bypass | io.stall_from_ca | io.stall_from_branch_bypass | io.stall_from_dcache | io.stall_from_alu_bypass
        io.stall_inst_if_id := io.stall_from_inst_if | io.stall_from_alu | io.stall_from_mem | io.stall_from_mem_bypass | io.stall_from_ca | io.stall_from_branch_bypass | io.stall_from_dcache | io.stall_from_alu_bypass
        io.stall_id_ex := io.stall_from_alu | io.stall_from_mem | io.stall_from_mem_bypass | io.stall_from_ca | io.stall_from_dcache | io.stall_from_alu_bypass
        io.stall_ex_mem := io.stall_from_alu | io.stall_from_mem | io.stall_from_mem_bypass | io.stall_from_ca | io.stall_from_dcache | io.stall_from_alu_bypass
        io.stall_mem_ca := io.stall_from_mem | io.stall_from_ca | io.stall_from_dcache
        io.stall_ca_wb := io.stall_from_ca | io.stall_from_dcache
        io.stall_from_mem_reg := stall_mem_reg

        //io.flush_mem_ca := io.stall_from_mem
    }

    //io.flush_pc_inst_if := io.branch_jump | io.direct_jump
    io.flush_inst_if_id := io.branch_jump | io.direct_jump //| RegNext(io.stall_from_inst_if)
    io.flush_id_ex := io.branch_jump
    io.flush_ex_mem := false.B 
    io.flush_mem_ca := false.B 
    io.flush_ca_wb := false.B 
    
}
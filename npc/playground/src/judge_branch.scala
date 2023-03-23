import chisel3._
import chisel3.util._
import chisel3.experimental._

class judge_branch_m extends Module{
    val io = IO(new Bundle{
        val judge_branch = Input(UInt(1.W))
        val rs1_rdata = Input(UInt(64.W))
        val rs2_rdata = Input(UInt(64.W))
        val imm = Input(UInt(64.W))
        val inst_address = Input(UInt(64.W)) 
        val funct3 = Input(UInt(3.W))
        // 1 : branch jump; 0 : not
        val branch_jump = Output(UInt(1.W))
        val branch_jump_addr = Output(UInt(64.W))
    })

    val branch_jump_en = WireDefault(0.U(1.W))

    //equal ： 0；
    val xor_result = WireDefault(io.rs1_rdata ^ io.rs2_rdata)
    val less_than_result_S = WireDefault(io.rs1_rdata.asSInt < io.rs2_rdata.asSInt) 
    val less_than_result_U = WireDefault(io.rs1_rdata < io.rs2_rdata) 

    val equal = WireDefault(!xor_result)

    io.branch_jump := branch_jump_en & io.judge_branch
    io.branch_jump_addr := io.imm + io.inst_address

    switch (io.funct3){
        is ("b000".U){
            //beq
            branch_jump_en := equal
        }
        is ("b001".U){
            //bne
            branch_jump_en := !equal
        }
        is ("b101".U){
            //bge 
            branch_jump_en := !less_than_result_S
        }
        is ("b100".U){
            //blt 
            branch_jump_en := less_than_result_S
        }
        is ("b110".U){
            //bltu
            branch_jump_en := less_than_result_U
        }
        is ("b111".U){
            //bgeu
            branch_jump_en := !less_than_result_U
        }
    }
}
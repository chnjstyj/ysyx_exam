import chisel3._
import chisel3.util._
import chisel3.experimental._

class pc extends Module{
    val io = IO(new Bundle{
        val inst_address = Output(UInt(64.W))
        val next_inst_address = Output(UInt(64.W))
        val ce = Output(UInt(1.W))

        val direct_jump = Input(UInt(1.W))
        val direct_jump_addr = Input(UInt(64.W))

        val branch_jump = Input(UInt(1.W))
        val branch_jump_addr = Input(UInt(64.W))

        val ecall = Input(UInt(1.W))
        val ecall_addr = Input(UInt(64.W))

        val mret = Input(UInt(1.W))
        val mret_addr = Input(UInt(64.W))

        val stall_global = Input(UInt(1.W))
    })

    val ce = RegInit(0.U(1.W))
    val inst_address = Reg(UInt(64.W))
    io.inst_address := inst_address 
    val next_inst_address = WireDefault(inst_address + 4.U)
    io.next_inst_address := next_inst_address

    //ftrace signal
    val jump_signal = WireDefault(io.direct_jump)
    val call_ftrace_handle = Module(new call_ftrace_handle)
    call_ftrace_handle.io.jump_signal := jump_signal

    ce := 1.U
    when (ce === 0.U){
        inst_address := "h0000_0000_8000_0000".U 
    }.elsewhen(io.stall_global === 1.U){
        inst_address := inst_address
    }.elsewhen (io.direct_jump === 1.U){
        inst_address := io.direct_jump_addr
    }.elsewhen (io.branch_jump === 1.U){
        inst_address := io.branch_jump_addr
    }.elsewhen (io.ecall === 1.U){
        inst_address := io.ecall_addr
    }.elsewhen (io.mret === 1.U){
        inst_address := io.mret_addr
    }.otherwise{
        inst_address := next_inst_address
    }
    io.ce := ce

}
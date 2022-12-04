import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile
import chisel3.util.experimental.loadMemoryFromFileInline

class top extends Module{
    val io = IO(new Bundle{
        val inst = Output(UInt(32.W))
        val imm = Output(SInt(64.W))
    })

    val pc = Module(new pc) 
    val inst_if = Module(new inst_if) 
    val id = Module(new id(4))

    io.inst := inst_if.io.inst
    io.imm := id.io.imm

    inst_if.io.inst_address := pc.io.inst_address
    id.io.inst := inst_if.io.inst

}
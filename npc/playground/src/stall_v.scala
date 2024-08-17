import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.HasBlackBoxInline
import java.io.File


class stall_v extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        val clock = Input(Clock())
        val stall_from_inst_if = Input(UInt(1.W))
        val stall_from_mem = Input(UInt(1.W))

        val stall_global = Output(UInt(1.W))
    })

    addPath(new File("/home/tang/ysyx-workbench/npc/playground/src/stall.v").getCanonicalPath)
    /*
    withClock ((!clock).asClock){
        val stall_mem_reg = RegNext(io.stall_from_mem)
        val stall_inst_if_reg = RegNext(io.stall_from_inst_if)
        io.stall_global := stall_mem_reg | stall_inst_if_reg
    }
    */
}
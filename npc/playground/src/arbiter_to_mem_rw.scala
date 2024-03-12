import chisel3._
import chisel3.util._
import chisel3.experimental._
import java.io.File

class mem_read extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        val ACLK = Input(Clock())
        val ARESETn = Input(Bool())
        val addr = Input(UInt(32.W))
        val en = Input(Bool())
        val valid = Output(Bool())
        val rdata = Output(UInt(256.W))
    })

    addPath(new File("/home/tang/ysyx-workbench/npc/playground/src/mem_read.v").getCanonicalPath)
}

class mem_write extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        val ACLK = Input(Clock())
        val ARESETn = Input(Bool())
        val addr = Input(UInt(32.W))
        val en = Input(Bool())
        val wdata = Input(UInt(256.W))
        val wmask = Input(UInt(4.W))
        val finish = Output(Bool())
    })

    addPath(new File("/home/tang/ysyx-workbench/npc/playground/src/mem_write.v").getCanonicalPath)
}
import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile
import chisel3.util.experimental.loadMemoryFromFileInline
import chisel3.util.HasBlackBoxInline
import java.io.File

class csrs extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        val clock = Input(Clock())
        val csr_addr = Input(UInt(12.W))
        val rs1_rdata = Input(UInt(64.W))
        val csr_rdata = Output(UInt(64.W))
        val rd_wdata = Input(UInt(64.W))
        val csr_wen = Input(UInt(1.W))
        val csr_sen = Input(UInt(1.W))
    })
    addPath(new File("/home/tang/ysyx-workbench/npc/playground/src/csrs.v").getCanonicalPath)
}
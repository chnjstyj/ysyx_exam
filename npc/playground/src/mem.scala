import chisel3._
import chisel3.util._
import chisel3.experimental._
import java.io.File

class mem extends BlackBox with HasBlackBoxPath {
    val io = IO(new Bundle{
        val clock = Input(Clock())
        //mem write
        val mem_write_addr = Input(UInt(64.W))
        val mem_write_data = Input(UInt(64.W))
        val mem_write_en = Input(UInt(1.W))
        val mem_wmask = Input(UInt(4.W))
    })

    addPath(new File("/home/tang/ysyx-workbench/npc/playground/src/mem_rw.v").getCanonicalPath)
    addPath(new File("/home/tang/ysyx-workbench/npc/playground/src/mem.v").getCanonicalPath)

}
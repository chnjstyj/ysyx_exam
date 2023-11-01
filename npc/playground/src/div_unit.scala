import chisel3._
import chisel3.util._
import chisel3.experimental._
import java.io.File

class div_unit extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        val data_a = Input(UInt(64.W))
        val data_b = Input(UInt(64.W))
        val alu_result_size = Input(UInt(1.W))
        val result = Output(UInt(64.W))
    })

    addPath(new File("/home/tang/ysyx-workbench/npc/playground/src/div.v").getCanonicalPath)

}

import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile
import chisel3.util.experimental.loadMemoryFromFileInline

class if extends Module{
    val io = IO(new Bundle{
        val addr = Input(UInt(32.W))
    })
}
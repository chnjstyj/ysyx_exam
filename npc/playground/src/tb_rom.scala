import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile
import chisel3.util.experimental.loadMemoryFromFileInline

class tb_rom extends Module{
    val io = IO(new Bundle{
        val addr = Input(UInt(3.W))
        val dout = Output(UInt(8.W))
    })

    val dir = "./data.hex"
    val mem = Mem(8,UInt(8.W))
    when(true.B){
    loadMemoryFromFileInline(mem,dir)
    }

    io.dout := mem(io.addr)
}
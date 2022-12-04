import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile
import chisel3.util.experimental.loadMemoryFromFileInline

class inst_if extends Module{
    val io = IO(new Bundle{
        val inst_address = Input(UInt(32.W))
        val inst = Output(UInt(32.W))
    })

    val mem = Mem(6, UInt(32.W))

    loadMemoryFromFile(mem,"inst.rom")

    val inst_addr = WireDefault(0.U)
    inst_addr := (io.inst_address & "h7fff_ffff".U)>>2

    io.inst := mem(inst_addr)
}
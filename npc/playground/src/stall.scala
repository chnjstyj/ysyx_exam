import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.HasBlackBoxInline


class stall extends Module{
    val io = IO(new Bundle{
        val exit_debugging = Input(UInt(1.W))
        val stall_from_inst_if = Input(UInt(1.W))
        val stall_from_mem = Input(UInt(1.W))

        //
        val stall_global = Output(UInt(1.W))
    })

    val exit_verilator = Module(new exit_verilator)

    exit_verilator.io.clk := clock
    exit_verilator.io.exit_debugging := RegNext(io.exit_debugging)

    io.stall_global := io.stall_from_inst_if | io.stall_from_mem

}
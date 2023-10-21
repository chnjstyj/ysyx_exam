import chisel3._
import chisel3.util._
import chisel3.experimental._

class rem extends Module{
    val io = IO(new Bundle{
        val data_a = Input(UInt(64.W))
        val data_b = Input(UInt(64.W))
        val data_a_32 = Input(UInt(32.W))
        val data_b_32 = Input(UInt(32.W))
        val alu_result_size = Input(Bool())
        val result = Output(UInt(64.W))
    })

    val a = io.data_a.asSInt
    val b = io.data_b.asSInt
    val a_32 = io.data_a_32.asSInt
    val b_32 = io.data_b_32.asSInt
    val sresult = WireDefault(a.asSInt % b.asSInt)
    val sresult_32 = WireDefault(a_32.asSInt % b_32.asSInt)
    io.result := Mux(io.alu_result_size,Cat(Fill(32,0.U),sresult_32),sresult.asUInt)

}

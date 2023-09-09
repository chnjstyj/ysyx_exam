import chisel3._
import chisel3.util._
import chisel3.experimental._

class rem extends Module{
    val io = IO(new Bundle{
        val data_a = Input(UInt(64.W))
        val data_b = Input(UInt(64.W))
        val result = Output(UInt(64.W))
    })

    val a = io.data_a.asSInt
    val b = io.data_b.asSInt
    val sresult = WireDefault(a.asSInt % b.asSInt)
    io.result := sresult.asUInt

}

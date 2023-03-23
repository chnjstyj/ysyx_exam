import chisel3._
import chisel3.util._
import chisel3.experimental._

class mul extends Module{
    val io = IO(new Bundle{
        val funct3 = Input(UInt(3.W))
        val data_a = Input(UInt(64.W))
        val data_b = Input(UInt(64.W))
        val result = Output(UInt(64.W))
    })

    val a = WireDefault(io.data_a)
    val b = WireDefault(io.data_b)
    
    val mul_result = WireDefault((a * b)(63,0)) 
    val mulh_result = WireDefault(((a.asSInt * b.asSInt)(127,64)).asUInt) 
    val mulhsu_result = WireDefault(((a.asSInt * b.asUInt)(127,64)).asUInt) 
    val mulhu_result = WireDefault(((a * b)(127,64)).asUInt) 

    io.result := MuxCase("h0000_0000_0000_0000".U,Seq(
        (io.funct3 === "b000".U) -> (mul_result),
        (io.funct3 === "b001".U) -> (mulh_result),
        (io.funct3 === "b010".U) -> (mulhsu_result),
        (io.funct3 === "b011".U) -> (mulhu_result)
    ))


}
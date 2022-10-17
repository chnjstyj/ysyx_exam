import chisel3._
import chisel3.util._

class seg extends Module
{
    val io = IO(new Bundle
    {
        val input   = Input(UInt(4.W))
        val output  = Output(UInt(8.W))
    })

    io.output := "b1111_1111".U   

    switch (io.input)
    {
        is(0.U) {io.output := "b1100_0000".U}
        is(1.U) {io.output := "b1111_1001".U} 
        is(2.U) {io.output := "b1010_0100".U} 
        is(3.U) {io.output := "b1011_0000".U} 
        is(4.U) {io.output := "b1001_1001".U} 
        is(5.U) {io.output := "b1001_0010".U} 
        is(6.U) {io.output := "b1000_0010".U} 
        is(7.U) {io.output := "b1111_1000".U} 
        is(8.U) {io.output := "b1000_0000".U} 
        is(9.U) {io.output := "b1001_0000".U} 
        is(10.U){io.output := "b1000_1000".U}
        is(11.U){io.output := "b1000_0011".U}
        is(12.U){io.output := "b1100_0110".U}
        is(13.U){io.output := "b1010_0001".U}
        is(14.U){io.output := "b1000_0110".U}
        is(15.U){io.output := "b1000_1110".U}
    }
}
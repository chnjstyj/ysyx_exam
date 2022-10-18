import chisel3._
import chisel3.util._
import chisel3.experimental._

class tb_fifo extends Module{
    val io = IO(new Bundle{
        val din = Input(UInt(8.W))
        val write = Input(UInt(1.W))
        val dout = Output(UInt(8.W))
        val full = Output(UInt(1.W))
        val empty = Output(UInt(1.W))
    })

    val fifo = Module(new BubbleFifo(8,8))
    io.dout := 0.U
    io.full := fifo.io.enq.full
    io.empty := fifo.io.deq.empty
    fifo.io.enq.write := io.write
    when(~fifo.io.enq.full){
        fifo.io.enq.din := io.din
    }.otherwise{
        //fifo.io.enq.write := false.B
        fifo.io.enq.din := 0.U
    }

    when(~fifo.io.deq.empty){
        fifo.io.deq.read := true.B
        io.dout := fifo.io.deq.dout
    }.otherwise{
        fifo.io.deq.read := false.B
    }

}
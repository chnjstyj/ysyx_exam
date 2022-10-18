import chisel3._
import chisel3.util._
import chisel3.experimental._

class WriterIO(size: Int) extends Bundle { 
    val write = Input(Bool()) 
    val full = Output(Bool()) 
    val din = Input(UInt(size.W))
}

class ReaderIO(size: Int) extends Bundle { 
    val read = Input(Bool()) 
    val empty = Output(Bool()) 
    val dout = Output(UInt(size.W))
}

class FifoRegister(size:Int) extends Module{
    val io = IO(new Bundle{
        val enq = new WriterIO(size)
        val deq = new ReaderIO(size)
    })

    val empty :: full :: Nil = Enum(2) 
    val stateReg = RegInit(empty) 
    val dataReg = RegInit(0.U(size.W))

    when(stateReg === empty){
        when(io.enq.write){
            dataReg := io.enq.din 
            stateReg := full
        }
    }.elsewhen(stateReg === full){
        when(io.deq.read){
            dataReg := 0.U
            stateReg := empty
        }
    }.otherwise{

    }

    io.deq.dout := dataReg
    io.deq.empty := (stateReg === empty)
    io.enq.full := (stateReg === full)

}

class BubbleFifo(size:Int,depth:Int) extends Module{
    val io = IO(new Bundle{
        val enq = new WriterIO(size)
        val deq = new ReaderIO(size)
    })

    val buffers = Array.fill(depth) {Module(new FifoRegister(size))}

    for (i <- 0 until (depth -1)){
        buffers(i+1).io.enq.din := buffers(i).io.deq.dout
        buffers(i+1).io.enq.write := ~buffers(i).io.deq.empty
        buffers(i).io.deq.read := ~buffers(i+1).io.enq.full
    }

    io.enq <> buffers(0).io.enq
    io.deq <> buffers(depth -1).io.deq

}
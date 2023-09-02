import chisel3._
import chisel3.util._
import chisel3.experimental._
import java.io.File

class mem extends Module{ //BlackBox with HasBlackBoxPath {
    val io = IO(new Bundle{
        val ACLK = Input(Clock())
        val ARESETn = Input(Bool())
        val mem_addr = Input(UInt(64.W))
        val lsu_addr = Output(UInt(32.W)) 
        //mem write
        val mem_write_data = Input(UInt(64.W))
        val mem_write_en = Input(Bool())
        val mem_wmask = Input(UInt(4.W))
        //arbiter
        val mem_write_finish = Input(Bool())
        val lsu_write_en = Output(Bool())
        val lsu_write_data = Output(UInt(64.W))
        val lsu_write_mask = Output(UInt(4.W))
        //mem read
        val mem_read_en = Input(Bool())
        val mem_read_size = Input(UInt(4.W))
        val mem_read_data = Output(UInt(64.W))
        val zero_extends = Input(Bool())
        //arbiter
        val mem_read_valid = Input(Bool())
        val mem_rdata = Input(UInt(64.W))
        val lsu_read_en = Output(Bool())
        //stall 
        val stall_from_mem = Output(UInt(1.W))
    })

    //val valid = Wire(Bool())

    io.lsu_addr := io.mem_addr(31,0) 
    io.lsu_read_en := io.mem_read_en

    io.lsu_write_en := io.mem_write_en
    io.lsu_write_data := io.mem_write_data
    io.lsu_write_mask := io.mem_wmask

    when (!io.mem_read_valid && io.mem_read_en){
        io.stall_from_mem := 1.U 
    }.elsewhen (!io.mem_write_finish && io.mem_write_en){
        io.stall_from_mem := 1.U 
    }.otherwise{
        io.stall_from_mem := 0.U
    }

    io.mem_read_data := io.mem_rdata
    switch (io.mem_read_size){
        is ("b1000".U){
            io.mem_read_data := io.mem_rdata
        }
        is ("b0100".U){
            io.mem_read_data := Mux(io.zero_extends,Cat(Fill(32,0.U),io.mem_rdata(31,0)),Cat(Fill(32,io.mem_rdata(31)),io.mem_rdata(31,0)))
        }
        is ("b0010".U){
            io.mem_read_data := Mux(io.zero_extends,Cat(Fill(48,0.U),io.mem_rdata(15,0)),Cat(Fill(48,io.mem_rdata(15)),io.mem_rdata(15,0)))
        }
        is ("b0001".U){
            io.mem_read_data := Mux(io.zero_extends,Cat(Fill(56,0.U),io.mem_rdata(7,0)),Cat(Fill(56,io.mem_rdata(7)),io.mem_rdata(7,0)))
        }
    }

    /*
    addPath(new File("/home/tang/ysyx-workbench/npc/playground/src/mem_rw.v").getCanonicalPath)
    addPath(new File("/home/tang/ysyx-workbench/npc/playground/src/mem.v").getCanonicalPath)
    */
}
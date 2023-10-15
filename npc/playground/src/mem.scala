import chisel3._
import chisel3.util._
import chisel3.experimental._
import java.io.File

class mem extends Module{ //BlackBox with HasBlackBoxPath {
    val io = IO(new Bundle{
        val ACLK = Input(Clock())
        val ARESETn = Input(Bool())
        val mem_addr = Input(UInt(64.W))
        //val lsu_addr = Output(UInt(32.W)) 
        //mem write
        val mem_write_data = Input(UInt(64.W))
        val mem_write_en = Input(Bool())
        val mem_wmask = Input(UInt(4.W))
        //arbiter
        //val mem_write_finish = Input(Bool())
        //val lsu_write_en = Output(Bool())
        //val lsu_write_data = Output(UInt(64.W))
        //val lsu_write_mask = Output(UInt(4.W))
        //dcache 
        val dcache_write_en = Output(Bool()) 
        val dcache_write_data = Output(UInt(64.W)) 
        val dcache_write_mask = Output(UInt(4.W)) 
        val dcache_write_fin = Input(Bool())
        //mem read
        val mem_read_en = Input(Bool())
        val mem_read_size = Input(UInt(4.W))
        val mem_read_data = Output(UInt(64.W))
        val zero_extends = Input(Bool())
        //arbiter
        //val mem_read_valid = Input(Bool())
        //val mem_rdata = Input(UInt(64.W))
        //val lsu_read_en = Output(Bool())
        //dcache
        val dcache_read_addr = Output(UInt(32.W))
        val dcache_read_en = Output(Bool())
        val dcache_read_data = Input(UInt(64.W))
        val dcache_read_valid = Input(Bool())
        //direct access
        val direct_read_en = Output(Bool())
        val direct_read_data = Input(UInt(64.W)) 
        val direct_write_en = Output(Bool()) 
        val direct_write_data = Output(UInt(64.W))
        val direct_fin = Input(Bool())
        //stall 
        val stall_from_mem = Output(UInt(1.W))
    })

    //val valid = Wire(Bool())

    //io.lsu_addr := io.mem_addr(31,0) 
    //io.lsu_read_en := io.mem_read_en

    //io.lsu_write_en := io.mem_write_en
    //io.lsu_write_data := io.mem_write_data
    //io.lsu_write_mask := io.mem_wmask

    val device_read = WireDefault(io.mem_addr(29).asBool())

    io.dcache_read_addr := io.mem_addr(31,0) 
    io.dcache_read_en := io.mem_read_en & !device_read & !io.dcache_read_valid

    io.dcache_write_en := io.mem_write_en & !device_read & !io.dcache_write_fin
    io.dcache_write_data := io.mem_write_data 
    io.dcache_write_mask := io.mem_wmask   

    io.direct_read_en := io.mem_read_en & device_read
    io.direct_write_en := io.mem_write_en & device_read
    io.direct_write_data := io.mem_write_data

    when (!io.direct_fin && (io.direct_read_en | io.direct_write_en)){
        io.stall_from_mem := 1.U
    }.elsewhen (!io.dcache_read_valid && io.dcache_read_en){
        io.stall_from_mem := 1.U 
    }.elsewhen (!io.dcache_write_fin && io.dcache_write_en){
        io.stall_from_mem := 1.U 
    }.otherwise{
        io.stall_from_mem := 0.U
    }

    val read_data = WireDefault(0.U(64.W))
    read_data := Mux(io.direct_read_en,io.direct_read_data,io.dcache_read_data)
    io.mem_read_data := Mux(io.direct_read_en,io.direct_read_data,io.dcache_read_data)
    switch (io.mem_read_size){
        is ("b1000".U){
            io.mem_read_data := read_data
        }
        is ("b0100".U){
            io.mem_read_data := Mux(io.zero_extends,Cat(Fill(32,0.U),read_data(31,0)),Cat(Fill(32,read_data(31)),read_data(31,0)))
        }
        is ("b0010".U){
            io.mem_read_data := Mux(io.zero_extends,Cat(Fill(48,0.U),read_data(15,0)),Cat(Fill(48,read_data(15)),read_data(15,0)))
        }
        is ("b0001".U){
            io.mem_read_data := Mux(io.zero_extends,Cat(Fill(56,0.U),read_data(7,0)),Cat(Fill(56,read_data(7)),read_data(7,0)))
        }
    }

    /*
    addPath(new File("/home/tang/ysyx-workbench/npc/playground/src/mem_rw.v").getCanonicalPath)
    addPath(new File("/home/tang/ysyx-workbench/npc/playground/src/mem.v").getCanonicalPath)
    */
}
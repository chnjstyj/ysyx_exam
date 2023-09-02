import chisel3._ 
import chisel3.util._ 

class axi_lite_arbiter extends Module {
    val io = IO(new Bundle{
        val ACLK = Input(Clock())
        val ARESETn = Input(Bool())
        //ifu read
        val ifu_read_addr = Input(UInt(32.W))
        val ifu_read_en = Input(Bool()) 
        val ifu_read_valid = Output(Bool()) 
        val ifu_read_data = Output(UInt(64.W))
        //lsu read
        val lsu_addr = Input(UInt(32.W))
        val lsu_read_en = Input(Bool()) 
        val lsu_read_valid = Output(UInt(1.W)) 
        val lsu_read_data = Output(UInt(64.W))
        //lsu write
        val lsu_write_data = Input(UInt(64.W))
        val lsu_write_en = Input(UInt(1.W)) 
        val lsu_write_mask = Input(UInt(4.W))
        val lsu_write_finish = Output(UInt(1.W)) 
    })
    
    //s0 : idle
    //s1 : IFU
    //s2 : LSU
    //...
    val s0 :: s1 :: s2 :: Nil = Enum(3)

    val ifu_en = WireDefault(io.ifu_read_en)
    val lsu_en = WireDefault(io.lsu_read_en | io.lsu_write_en)

    val ifu_finish = WireDefault(io.ifu_read_valid)
    val lsu_finish = WireDefault(io.lsu_read_valid | io.lsu_write_finish)

    val next_state = WireDefault(s0)
    val cur_state = RegNext(next_state,s0) 

    val addr = RegInit(0.U(32.W))
    val mem_read_en = RegInit(false.B)
    val mem_write_en = RegInit(false.B)
    val mem_rdata = WireDefault(0.U(64.W)) 
    val mem_read_valid = WireDefault(false.B) 
    val mem_write_finish = WireDefault(false.B) 
    val mem_wdata = RegNext(io.lsu_write_data,0.U(64.W)) 
    val mem_wmask = RegNext(io.lsu_write_mask,0.U(4.W))   

    val arbiter_to_mem_read = Module(new mem_read)
    val arbiter_to_mem_write = Module(new mem_write)

    switch (cur_state){
        is (s0){
            when (ifu_en === 1.U){
                next_state := s1 
            }.elsewhen (lsu_en === 1.U){
                next_state := s2 
            }.otherwise{
                next_state := s0 
            }
        }
        is (s1){
            when ((ifu_finish & lsu_en) === 1.U){
                next_state := s2 
            }.elsewhen (ifu_en === 1.U){
                next_state := s1 
            }.otherwise{
                next_state := s0 
            }
        }
        is (s2){
            when ((lsu_finish & ifu_en) === 1.U){
                next_state := s1 
            }.elsewhen (lsu_finish === 1.U){
                next_state := s0
            }.otherwise{
                next_state := s2 
            }
        }
    }

    io.ifu_read_valid := Mux(cur_state === s1,mem_read_valid,false.B)
    io.ifu_read_data := Mux(cur_state === s1,mem_rdata,0.U(64.W))
    io.lsu_read_valid := Mux(cur_state === s2,mem_read_valid,false.B)
    io.lsu_read_data := Mux(cur_state === s1,mem_rdata,0.U(64.W))
    io.lsu_write_finish := mem_write_finish

    when (cur_state === s0){
        addr := 0.U 
        mem_read_en := 0.U
        mem_write_en := 0.U 
        mem_wdata := 0.U 
        mem_wmask := 0.U 
    }.elsewhen (cur_state === s1){
        addr := io.ifu_read_addr
        mem_read_en := 1.U 
        mem_write_en := 0.U 
    }.elsewhen (cur_state === s2){
        addr := io.lsu_addr 
        mem_wdata := io.lsu_write_data
        mem_wmask := io.lsu_write_mask
        when (io.lsu_read_en){
            mem_read_en := 1.U 
            mem_write_en := 0.U 
        }.otherwise{
            mem_read_en := 0.U 
            mem_write_en := 1.U 
        }
    }

    arbiter_to_mem_read.io.ACLK := io.ACLK 
    arbiter_to_mem_read.io.ARESETn := io.ARESETn 
    arbiter_to_mem_read.io.addr := addr 
    arbiter_to_mem_read.io.en := mem_read_en
    mem_read_valid := arbiter_to_mem_read.io.valid 
    mem_rdata := arbiter_to_mem_read.io.rdata

    arbiter_to_mem_write.io.ACLK := io.ACLK 
    arbiter_to_mem_write.io.ARESETn := io.ARESETn 
    arbiter_to_mem_write.io.addr := addr 
    arbiter_to_mem_write.io.en := mem_write_en 
    arbiter_to_mem_write.io.wdata := mem_wdata 
    arbiter_to_mem_write.io.wmask := mem_wmask 
    mem_write_finish := arbiter_to_mem_write.io.finish

}
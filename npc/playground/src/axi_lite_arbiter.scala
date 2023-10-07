import chisel3._ 
import chisel3.util._ 

class axi_lite_arbiter(
    offset_width:Int
) extends Module {
    val io = IO(new Bundle{
        val ACLK = Input(Clock())
        val ARESETn = Input(Bool())
        //ifu read
        val ifu_read_addr = Input(UInt(32.W))
        val ifu_read_en = Input(Bool()) 
        val ifu_read_valid = Output(Bool()) 
        val ifu_read_data = Output(UInt((1 << (offset_width + 3)).W))
        //lsu read
        val lsu_addr = Input(UInt(32.W))
        val lsu_read_en = Input(Bool()) 
        val lsu_read_valid = Output(Bool()) 
        val lsu_read_data = Output(UInt((1 << (offset_width + 3)).W))
        //lsu write
        val lsu_write_data = Input(UInt((1 << (offset_width + 3)).W))
        val lsu_write_en = Input(Bool()) 
        val lsu_write_finish = Output(UInt(1.W)) 
    })
    
    //s0 : idle
    //s1 : IFU
    //s2 : LSU
    //...
    val s0 :: s1 :: s2 :: Nil = Enum(3)

    val icache_read_data = RegInit(0.U((1 << (offset_width + 3)).W))
    val icache_read_counter = RegInit(0.U(((1 << (offset_width - 3)) - 2).W))
    val icache_read_data_fin = RegInit(false.B)
    val icache_read_addr = Wire(UInt(32.W))

    val dcache_read_data = RegInit(0.U((1 << (offset_width + 3)).W)) 
    val dcache_read_counter = RegInit(0.U(((1 << (offset_width - 3)) - 2).W)) 
    val dcache_read_data_fin = RegInit(false.B) 
    val dcache_read_addr = Wire(UInt(32.W))

    val ifu_en = WireDefault(io.ifu_read_en)
    val lsu_en = WireDefault(io.lsu_read_en | io.lsu_write_en)

    val ifu_finish = WireDefault(icache_read_data_fin)
    val lsu_finish = WireDefault(io.lsu_read_valid | io.lsu_write_finish)

    val next_state = WireDefault(s0)
    val cur_state = RegNext(next_state,s0) 

    val addr = WireDefault(0.U(32.W))
    val mem_read_en = RegInit(false.B)
    val mem_write_en = RegInit(false.B)
    val mem_rdata = WireDefault(0.U(64.W)) 
    //val mem_rdata_r = RegNext(RegNext(mem_rdata))
    val mem_read_valid = RegInit(false.B) 
    val mem_write_finish = RegInit(false.B)

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

    io.ifu_read_valid := Mux(cur_state === s1,icache_read_data_fin ,false.B)
    io.ifu_read_data := Mux(cur_state === s1,icache_read_data,0.U((1 << (offset_width + 3)).W))
    io.lsu_read_valid := Mux(cur_state === s2,mem_read_valid ,false.B)
    io.lsu_read_data := Mux(cur_state === s2,mem_rdata,0.U(64.W))
    io.lsu_write_finish := mem_write_finish

    when (next_state === s0){
        //addr := 0.U 
        mem_read_en := 0.U
        mem_write_en := 0.U 
    }.elsewhen (next_state === s1){
        //addr := icache_read_addr
        //when (mem_read_valid)
        //{
        //    mem_read_en := 0.U 
        //    mem_write_en := 0.U 
        //}.otherwise{
            mem_read_en := 1.U 
            mem_write_en := 0.U 
        //}
    }.elsewhen (next_state === s2){
        //addr := io.lsu_addr 
        when (io.lsu_read_en){
            mem_read_en := 1.U 
            mem_write_en := 0.U 
        }.elsewhen (io.lsu_write_en){
            mem_read_en := 0.U 
            mem_write_en := 1.U 
        }
    }

    
    when (cur_state === s0){
        addr := 0.U 
    }.elsewhen (cur_state === s1){
        addr := icache_read_addr
    }.elsewhen (cur_state === s2){
        addr := dcache_read_addr 
    }.otherwise{
        addr := 0.U
    }

    val counter_end = ((1 << (offset_width - 3)) - 1)

    icache_read_addr := io.ifu_read_addr + (icache_read_counter << 3.U)
    when (next_state === s1){
        //icache_read_addr := io.ifu_read_addr + (icache_read_counter << 3.U) //next 64 bits block
        when(mem_read_valid && icache_read_counter < counter_end.U){
            icache_read_data_fin := false.B 
            icache_read_counter := icache_read_counter + 1.U 
            icache_read_data := icache_read_data | (mem_rdata << (icache_read_counter << 6.U))
        }.elsewhen (mem_read_valid && icache_read_counter === counter_end.U){
            //last one to read
            icache_read_data_fin := true.B 
            icache_read_data := icache_read_data | (mem_rdata << (icache_read_counter << 6.U))
            icache_read_counter := 0.U
        }.otherwise{
            icache_read_data_fin := false.B 
        }
    }.otherwise{
        icache_read_data_fin := false.B 
        //icache_read_addr := 0.U 
        icache_read_counter := 0.U 
        icache_read_data := 0.U 
    }

    dcache_read_addr := io.lsu_addr + (dcache_read_counter << 3.U)

    arbiter_to_mem_read.io.ACLK := io.ACLK 
    arbiter_to_mem_read.io.ARESETn := io.ARESETn 
    arbiter_to_mem_read.io.addr := addr 
    arbiter_to_mem_read.io.en := mem_read_en & !lsu_finish
    mem_read_valid := arbiter_to_mem_read.io.valid 
    mem_rdata := arbiter_to_mem_read.io.rdata

    arbiter_to_mem_write.io.ACLK := io.ACLK 
    arbiter_to_mem_write.io.ARESETn := io.ARESETn 
    arbiter_to_mem_write.io.addr := addr 
    arbiter_to_mem_write.io.en := mem_write_en & !lsu_finish
    arbiter_to_mem_write.io.wdata := io.lsu_write_data
    arbiter_to_mem_write.io.wmask := io.lsu_write_mask
    mem_write_finish := arbiter_to_mem_write.io.finish

}
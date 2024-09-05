import chisel3._ 
import chisel3.util._ 


package AXI{
    val axi_width:Int = 32
    class axi_master_aw extends Bundle{
        val master_awready = Output(Bool())
        val master_awvalid = Output(Bool())
        val master_awaddr = Output(UInt(32.W))
        val master_awid = Output(UInt(4.W))
        val master_awlen = Output(UInt(8.W))
        val master_awsize = Output(UInt(3.W))
        val master_awburst = Output(UInt(2.W))
    }

    class axi_master_w extends Bundle{
        val master_wready = Input(Bool())
        val master_wvalid = Output(Bool())
        val master_wdata = Output(UInt(32.W))
        val master_wstrb = Output(UInt(4.W))
        val master_wlast = Output(Bool()) 
    }

    class axi_master_b extends Bundle{
        val master_bready = Output(Bool())
        val master_bvalid = Input(Bool())
        val master_bresp = Input(UInt(2.W))
        val master_bid = Input(UInt(4.W))
    }

    class axi_master_ar extends Bundle{
        val master_arready = Input(Bool())
        val master_arvalid = Output(Bool())
        val master_araddr = Output(UInt(32.W))
        val master_arid = Output(UInt(4.W))
        val master_arlen = Output(UInt(8.W))
        val master_arsize = Output(UInt(3.W))
        val master_arburst = Output(UInt(2.W))
    }

    class axi_master_r extends Bundle{
        val master_rready = Output(Bool())
        val master_rvalid = Input(Bool())
        val master_rresp = Input(UInt(2.W))
        val master_rdata = Input(UInt(32.W))
        val master_rlast = Input(Bool())
        val master_rid = Input(UInt(4.W))
    }

    class axi_slave_aw extends Bundle{
        val slave_awready = Output(Bool())
        val slave_awvalid = Input(Bool())
        val slave_awaddr = Input(UInt(32.W))
        val slave_awid = Input(UInt(4.W))
        val slave_awlen = Input(UInt(8.W))
        val slave_awsize = Input(UInt(3.W))
        val slave_awburst = Input(UInt(2.W))
    }

    class axi_slave_w extends Bundle{
        val slave_wready = Input(Bool())
        val slave_wvalid = Output(Bool())
        val slave_wdata = Input(UInt(32.W))
        val slave_wstrb = Input(UInt(4.W))
        val slave_wlast = Input(Bool())
    }

    class axi_slave_b extends Bundle{
        val slave_bready = Input(Bool())
        val slave_bvalid = Output(Bool())
        val slave_bresp = Output(UInt(2.W))
        val slave_bid = Output(UInt(4.W))
    }

    class axi_slave_ar extends Bundle{
        val slave_arready = Output(Bool())
        val slave_arvalid = Input(Bool())
        val slave_araddr = Input(UInt(32.W))
        val slave_arid = Input(UInt(4.W))
        val slave_arlen = Input(UInt(8.W))
        val slave_arsize = Input(UInt(3.W))
        val slave_arburst = Input(UInt(2.W))
    }

    class axi_slave_r extends Bundle{
        val slave_rready = Input(Bool())
        val slave_rvalid = Output(Bool())
        val slave_rresp = Output(UInt(2.W))
        val slave_rdata = Output(UInt(32.W))
        val slave_rlast = Output(Bool())
        val slave_rid = Output(UInt(4.W))
    }

    class axi_master extends Bundle{
        val master_awready = Output(Bool())
        val master_awvalid = Output(Bool())
        val master_awaddr = Output(UInt(32.W))
        val master_awid = Output(UInt(4.W))
        val master_awlen = Output(UInt(8.W))
        val master_awsize = Output(UInt(3.W))
        val master_awburst = Output(UInt(2.W))

        val master_wready = Input(Bool())
        val master_wvalid = Output(Bool())
        val master_wdata = Output(UInt(32.W))
        val master_wstrb = Output(UInt(4.W))
        val master_wlast = Output(Bool()) 

        val master_bready = Output(Bool())
        val master_bvalid = Input(Bool())
        val master_bresp = Input(UInt(2.W))
        val master_bid = Input(UInt(4.W))
        
        val master_arready = Input(Bool())
        val master_arvalid = Output(Bool())
        val master_araddr = Output(UInt(32.W))
        val master_arid = Output(UInt(4.W))
        val master_arlen = Output(UInt(8.W))
        val master_arsize = Output(UInt(3.W))
        val master_arburst = Output(UInt(2.W))

        val master_rready = Output(Bool())
        val master_rvalid = Input(Bool())
        val master_rresp = Input(UInt(2.W))
        val master_rdata = Input(UInt(32.W))
        val master_rlast = Input(Bool())
        val master_rid = Input(UInt(4.W))
    }

    class axi_slave extends Bundle{
        val slave_awready = Output(Bool())
        val slave_awvalid = Input(Bool())
        val slave_awaddr = Input(UInt(32.W))
        val slave_awid = Input(UInt(4.W))
        val slave_awlen = Input(UInt(8.W))
        val slave_awsize = Input(UInt(3.W))
        val slave_awburst = Input(UInt(2.W))
        
        val slave_wready = Output(Bool())
        val slave_wvalid = Input(Bool())
        val slave_wdata = Input(UInt(32.W))
        val slave_wstrb = Input(UInt(4.W))
        val slave_wlast = Input(Bool())
        
        val slave_bready = Input(Bool())
        val slave_bvalid = Output(Bool())
        val slave_bresp = Output(UInt(2.W))
        val slave_bid = Output(UInt(4.W))
        
        val slave_arready = Output(Bool())
        val slave_arvalid = Input(Bool())
        val slave_araddr = Input(UInt(32.W))
        val slave_arid = Input(UInt(4.W))
        val slave_arlen = Input(UInt(8.W))
        val slave_arsize = Input(UInt(3.W))
        val slave_arburst = Input(UInt(2.W)) 
        
        val slave_rready = Input(Bool())
        val slave_rvalid = Output(Bool())
        val slave_rresp = Output(UInt(2.W))
        val slave_rdata = Output(UInt(32.W))
        val slave_rlast = Output(Bool())
        val slave_rid = Output(UInt(4.W))
    }
}

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
        //lsu direct read/write
        val lsu_direct_read_en = Input(Bool())  
        val lsu_direct_write_en = Input(Bool())
        val lsu_direct_write_data = Input(UInt(64.W))
        val lsu_direct_write_mask = Input(UInt(4.W))
        val lsu_direct_read_data = Output(UInt(64.W)) 
        val lsu_direct_fin = Output(Bool())
        val lsu_direct_addr = Input(UInt(32.W))
    })
    
    //s0 : idle
    //s1 : IFU
    //s2 : LSU
    //s3 : direct read/write
    val s0 :: s1 :: s2 :: s3 :: Nil = Enum(4)

    val icache_read_data = RegInit(0.U((1 << (offset_width + 3)).W))
    val icache_read_counter = RegInit(0.U(((1 << (offset_width - 3)) - 2).W))
    val icache_read_data_fin = RegInit(false.B)
    val icache_read_addr = Wire(UInt(32.W))

    val dcache_read_data = RegInit(0.U((1 << (offset_width + 3)).W)) 
    val dcache_read_counter = RegInit(0.U(((1 << (offset_width - 3)) - 2).W)) 
    val dcache_read_data_fin = RegInit(false.B) 
    val dcache_read_addr = Wire(UInt(32.W))

    val dcache_write_counter = RegInit(0.U(((1 << (offset_width - 3)) - 2).W)) 
    val dcache_write_data_fin = RegInit(false.B) 
    val dcache_write_data = WireDefault(0.U(256.W))

    val lsu_direct_fin = RegInit(false.B)

    val ifu_en = WireDefault(io.ifu_read_en)
    val lsu_en = WireDefault(io.lsu_read_en | io.lsu_write_en)
    val dir_en = WireDefault(io.lsu_direct_read_en | io.lsu_direct_write_en)

    val ifu_finish = WireDefault(icache_read_data_fin)
    val lsu_finish = WireDefault(dcache_read_data_fin | dcache_write_data_fin)
    val dir_finish = WireDefault(io.lsu_direct_fin)

    val next_state = WireDefault(s0)
    val cur_state = RegNext(next_state,s0) 

    val addr = WireDefault(0.U(32.W))
    val mem_read_en = RegInit(false.B)
    val mem_write_en = RegInit(false.B)
    val mem_rdata = WireDefault(0.U(256.W)) 
    //val mem_rdata_r = RegNext(RegNext(mem_rdata))
    val mem_read_valid = RegInit(false.B) 
    val mem_write_finish = RegInit(false.B)

    val arbiter_to_mem_read = Module(new mem_read)
    val arbiter_to_mem_write = Module(new mem_write)

    val direct_write_data = RegNext(io.lsu_direct_write_data,0.U)
    val direct_write_mask = RegNext(io.lsu_direct_write_mask,0.U)
    val direct_addr = RegNext(io.lsu_direct_addr,0.U)

    switch (cur_state){
        is (s0){
            when (ifu_en === 1.U){
                next_state := s1 
            }.elsewhen (lsu_en === 1.U){
                next_state := s2 
            }.elsewhen (dir_en === 1.U){
                next_state := s3 
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
        is (s3){
            when ((dir_finish & ifu_en) === 1.U){
                next_state := s1 
            }.elsewhen (dir_finish === 1.U && dir_en){
                next_state := s3
            }.elsewhen (dir_finish === 1.U){
                next_state := s0
            }.otherwise{
                next_state := s3 
            }
        }
    }

    io.ifu_read_valid := Mux(cur_state === s1,icache_read_data_fin ,false.B)
    io.ifu_read_data := Mux(cur_state === s1,icache_read_data,0.U((1 << (offset_width + 3)).W))
    io.lsu_read_valid := Mux(cur_state === s2,dcache_read_data_fin ,false.B)
    io.lsu_read_data := Mux(cur_state === s2,dcache_read_data,0.U((1 << (offset_width + 3)).W))
    io.lsu_write_finish := dcache_write_data_fin
    //io.lsu_direct_fin := Mux(cur_state === s3,mem_read_valid | mem_write_finish,false.B)
    io.lsu_direct_read_data := Mux(cur_state === s3,mem_rdata,0.U(64.W))

    when (next_state === s0){
        mem_read_en := 0.U
        mem_write_en := 0.U 
    }.elsewhen (next_state === s1){
            mem_read_en := 1.U 
            mem_write_en := 0.U 
    }.elsewhen (next_state === s2){
        when (io.lsu_read_en){
            mem_read_en := 1.U 
            mem_write_en := 0.U 
        }.elsewhen (io.lsu_write_en){
            mem_read_en := 0.U 
            mem_write_en := 1.U 
        }
    }.elsewhen (next_state === s3){
        /*
        when (io.lsu_direct_read_en){
            mem_read_en := 1.U 
            mem_write_en := 0.U 
        }.elsewhen (io.lsu_direct_write_en){
            mem_read_en := 0.U 
            mem_write_en := 1.U 
        }*/
    }
    
    when (cur_state === s0){
        addr := 0.U 
    }.elsewhen (cur_state === s1){
        when (next_state === s2){
            addr := dcache_read_addr
        }.otherwise{
            addr := icache_read_addr
        }
    }.elsewhen (cur_state === s2){
        addr := dcache_read_addr 
    }.otherwise{
        addr := 0.U
    }

    val counter_end = (1 << (offset_width + 3)) / AXI.axi_width

    icache_read_addr := io.ifu_read_addr + (icache_read_counter << 3.U)
    when (cur_state === s1){
        //icache_read_addr := io.ifu_read_addr + (icache_read_counter << 3.U) //next 64 bits block
        when(mem_read_valid && icache_read_counter < counter_end.U){
            //icache_read_data_fin := false.B 
            icache_read_counter := icache_read_counter + 1.U 
            icache_read_data := icache_read_data | (mem_rdata << (icache_read_counter << 6.U))
        }.elsewhen (mem_read_valid && icache_read_counter === counter_end.U){
            //last one to read
            //icache_read_data_fin := true.B 
            icache_read_data := icache_read_data | (mem_rdata << (icache_read_counter << 6.U))
            icache_read_counter := 0.U
        }.otherwise{
            //icache_read_data_fin := false.B 
        }
    }.otherwise{
        //icache_read_data_fin := false.B 
        //icache_read_addr := 0.U 
        icache_read_counter := 0.U 
        icache_read_data := 0.U 
    }

    when (mem_read_valid && icache_read_counter === counter_end.U && cur_state === s1){
        icache_read_data_fin := true.B 
    }.otherwise{
        icache_read_data_fin := false.B
    }

    dcache_read_addr := io.lsu_addr + Mux(io.lsu_write_en,(dcache_write_counter << 3.U),(dcache_read_counter << 3.U))
    when (cur_state === s2 && mem_read_en.asBool()){
        when (mem_read_valid && dcache_read_counter < counter_end.U){
            dcache_read_data_fin := false.B 
            dcache_read_counter := dcache_read_counter + 1.U 
            dcache_read_data := dcache_read_data | (mem_rdata << (dcache_read_counter << 6.U))
        }.elsewhen (mem_read_valid && dcache_read_counter === counter_end.U){
            //last one to read
            dcache_read_data_fin := true.B 
            dcache_read_data := dcache_read_data | (mem_rdata << (dcache_read_counter << 6.U))
            dcache_read_counter := 0.U
        }.otherwise{
            dcache_read_data_fin := false.B 
        }
    }.otherwise{
        dcache_read_data_fin := false.B 
        //dcache_read_addr := 0.U 
        dcache_read_counter := 0.U 
        dcache_read_data := 0.U 
    }

    when (next_state === s2 && mem_write_en.asBool()){
        dcache_write_data := (io.lsu_write_data >> (dcache_write_counter << 6.U))
        when (mem_write_finish && dcache_write_counter < counter_end.U){
            dcache_write_counter := dcache_write_counter + 1.U 
            dcache_write_data_fin := false.B 
        }.elsewhen (mem_write_finish && dcache_write_counter === counter_end.U){
            //last one to write
            dcache_write_data_fin := true.B 
            dcache_write_counter := 0.U 
        }.otherwise{
            dcache_write_data_fin := false.B 
        }
    }.elsewhen (next_state === s3 && io.lsu_direct_write_en){
        dcache_write_data := io.lsu_direct_write_data
        dcache_write_counter := 0.U 
        dcache_write_data_fin := false.B 
    }.otherwise{
        dcache_write_counter := 0.U 
        dcache_write_data_fin := false.B 
        dcache_write_data := 0.U
    }

    io.lsu_direct_fin := (mem_write_finish | mem_read_valid) && cur_state === s3
    /*
    when (next_state === s3 && io.lsu_direct_write_en){
        when (mem_write_finish){
            lsu_direct_fin := true.B 
        }.otherwise{
            lsu_direct_fin := false.B 
        }
    }.elsewhen (next_state === s3 && io.lsu_direct_read_en){
        when (mem_read_valid){
            lsu_direct_fin := true.B 
        }.otherwise{
            lsu_direct_fin := false.B 
        }
    }.otherwise{
        lsu_direct_fin := false.B
    }*/

    arbiter_to_mem_read.io.ACLK := io.ACLK 
    arbiter_to_mem_read.io.ARESETn := io.ARESETn 
    arbiter_to_mem_read.io.addr := Mux(next_state === s3,io.lsu_direct_addr,addr) 
    arbiter_to_mem_read.io.en := Mux(next_state === s3,io.lsu_direct_read_en,mem_read_en & !lsu_finish)
    arbiter_to_mem_read.io.length := Mux(next_state === s3,0.U,(counter_end - 1).U)
    mem_read_valid := arbiter_to_mem_read.io.valid 
    mem_rdata := arbiter_to_mem_read.io.rdata

    arbiter_to_mem_write.io.ACLK := io.ACLK 
    arbiter_to_mem_write.io.ARESETn := io.ARESETn 
    arbiter_to_mem_write.io.addr := Mux(next_state === s3,io.lsu_direct_addr,addr) 
    arbiter_to_mem_write.io.en := Mux(next_state === s3,io.lsu_direct_write_en,mem_write_en & !lsu_finish & !mem_write_finish)
    arbiter_to_mem_write.io.wdata := Mux(next_state === s3,io.lsu_direct_write_data,dcache_write_data)
    arbiter_to_mem_write.io.wmask := Mux(next_state === s3,io.lsu_direct_write_mask,"b1000".U)
    mem_write_finish := arbiter_to_mem_write.io.finish

}
import chisel3._
import chisel3.util._
import chisel3.experimental._
/*
采用状态机
空闲
比对     如果Hit 则转为空闲，如果miss，且dirty为1，转到写回，否则转到替换
写回     完成后转到替换
替换     完成后返回比对
跨行     完成后转到比对
*/
class icache_controller(
    tag_width:Int,
    index_width:Int,
    offset_width:Int,
    ways:Int 
) extends Module{
    val io = IO(new Bundle{
        val addr = Input(UInt(32.W))
        val read_cache_en = Input(Bool())
        val read_cache_size = Input(UInt(4.W))
        val write_cache_en = Input(Bool()) 
        val mem_write_fin = Input(Bool())
        val mem_read_fin = Input(Bool())
        val cache_miss = Output(Bool())
        val cache_data = Output(UInt(64.W))
        val mem_read_en = Output(Bool())
        val mem_write_en = Output(Bool()) //used for substitute write signal 
        val mem_read_data = Input(UInt((1 << (offset_width + 3)).W))
        val mem_addr = Output(UInt(32.W))
        val write_cache_data = Input(UInt(64.W))
        val write_cache_mask = Input(UInt(4.W))
        val cache_writeback_data = Output(UInt((1 << (offset_width + 3)).W)) //used for substitute write data 
        val read_cache_fin = Output(Bool())
        val write_cache_fin = Output(Bool())
        val crossline_access_stall = Output(Bool())
        val offset1 = Output(UInt(6.W))
    })

    val cache = Module(new cache(tag_width,index_width,offset_width,ways))

    val s0 :: s1 :: s2 :: s3 :: s4 :: Nil = Enum(5)

    val addr_offset = WireDefault(io.addr(offset_width-1,0))
    
    val next_state = WireDefault(s0)
    val cur_state = RegNext(next_state,s0)

    val write_miss = Wire(Bool())
    val write_hit = Wire(Bool())
    val read_miss = Wire(Bool())
    val read_hit = Wire(Bool())
    val dirty_bit = Wire(Bool())
    val substitude = Wire(Bool())
    val substitude_data = WireDefault(io.mem_read_data)
    val substitude_fin = Wire(Bool())
    
    val offset1 = Wire(UInt(6.W))
    val access_cache_size_6 = Wire(UInt(6.W))
    val addr_offset_6 = Wire(UInt(6.W))
    addr_offset_6 := addr_offset

    val cache_curline_data_ready = RegInit(false.B)
    val cache_curline_data = RegInit(0.U(64.W))
    val cache_curline_write_mask = WireDefault((1 << offset_width).U - addr_offset_6)
    val write_mask = Wire(UInt(4.W))
    val write_data = Wire(UInt(64.W))

    val cache_nextline_addr = WireDefault((io.addr + (1 << (offset_width)).U) & (~((1 << (offset_width)) - 1).U(32.W)))
    val cache_nextline_data = Wire(UInt(64.W))
    cache_nextline_data := cache.io.read_data
    val crossline_access = RegInit(false.B) 

    val cache_writeback_addr = RegInit(0.U(32.W))
    val cache_writeback_index = RegInit(0.U(ways.W))
    val cache_writeback_data = RegInit(0.U((1 << (offset_width + 3)).W))
    io.cache_writeback_data := cache_writeback_data

    io.read_cache_fin := Mux(crossline_access & !read_hit,false.B,read_hit) 
    io.write_cache_fin := Mux(crossline_access,false.B,write_hit) 
    //io.mem_addr := Mux(cur_state === s2,cache.io.writeback_addr,io.addr & (~((1 << (offset_width)) - 1).U(32.W)))

    when (next_state === s2 && cur_state =/= s1){
        cache_writeback_addr := cache.io.writeback_addr
        cache_writeback_index := cache.io.writeback_index
        cache_writeback_data := cache.io.writeback_data
    }

    when (cur_state === s2){
        io.mem_addr := cache.io.writeback_addr
    }.elsewhen (cur_state === s3 && crossline_access && cache_curline_data_ready){
        io.mem_addr := cache_nextline_addr
    }.otherwise{
        io.mem_addr := io.addr & (~((1 << (offset_width)) - 1).U(32.W))
    }

    when (io.write_cache_en){
        access_cache_size_6 := io.write_cache_mask
    }.otherwise{
        access_cache_size_6 := io.read_cache_size
    }
    offset1 := (access_cache_size_6 + addr_offset_6)
    io.offset1 := offset1

    when (next_state === s4){
        write_mask := cache_curline_write_mask
        write_data := io.write_cache_data
    }.elsewhen (next_state === s1 && crossline_access){
        write_mask := offset1 - (1 << offset_width).U
        write_data := io.write_cache_data >> ((io.write_cache_mask - cache_curline_write_mask) << 3)
    }.otherwise{
        write_mask := io.write_cache_mask
        write_data := io.write_cache_data
    }

    switch(cur_state){
        is (s0){
            when ((io.read_cache_en || io.write_cache_en) && ((offset1) > (1 << offset_width).U)){
                next_state := s4
            }.elsewhen (io.read_cache_en || io.write_cache_en){
                next_state := s1
            }.otherwise{
                next_state := s0
            }
        }
        is (s1){
            when ((write_miss || read_miss) && dirty_bit){
                next_state := s2
            }.elsewhen ((write_miss || read_miss) && (io.read_cache_en || io.write_cache_en)){
                next_state := s3
            }.elsewhen ((io.read_cache_en || io.write_cache_en) && ((offset1) > (1 << offset_width).U && !cache_curline_data_ready)){
                next_state := s4
            }.elsewhen (io.read_cache_en || io.write_cache_en){
                next_state := s1
            }.otherwise{
                next_state := s1
            }
        }
        is (s2){
            when (io.mem_write_fin){
                next_state := s3
            }.otherwise{
                next_state := s2
            }
        }
        is (s3){
            when (substitude_fin){
                when (crossline_access && !cache_curline_data_ready){
                    next_state := s4
                }.otherwise{
                    next_state := s1
                }
            }.otherwise{
                next_state := s3
            }
        }
        is (s4){
            when ((write_miss || read_miss) && dirty_bit){
                next_state := s2 
            }.elsewhen ((write_miss || read_miss) && (io.read_cache_en || io.write_cache_en)){
                next_state := s3
            }.elsewhen ((read_hit || write_hit) && cache_curline_data_ready){
                //read next line
                next_state := s1 
            }.otherwise{
                next_state := s4
            }
        }
    }

    when (next_state === s4){
        crossline_access := true.B
    }.elsewhen ((read_hit || write_hit) && next_state === s1){
        crossline_access := false.B
    }

    when (cur_state === s4 && (read_hit || write_hit)){
        cache_curline_data_ready := true.B 
    }.elsewhen (cur_state === s1){
        cache_curline_data_ready := false.B
    }

    withClock (read_hit.asClock){
        when (next_state === s4 && read_hit){
            cache_curline_data := cache.io.read_data //>> ((addr_offset_6 + read_cache_size_6 - (1 << offset_width).U) << 3)
        }
    }

    io.crossline_access_stall := cur_state === s4

    val cache_combined_data = Wire(UInt(64.W))
    cache_combined_data := cache_curline_data | (cache_nextline_data << (((1 << offset_width).U - addr_offset_6) << 3))
    io.cache_data := Mux(cache_curline_data_ready,cache_combined_data,cache.io.read_data)

    when (next_state === s1){
        io.cache_miss := false.B
    }.otherwise{
        io.cache_miss := true.B
    }

    val cache_read_en  = RegInit(false.B)
    val cache_write_en = RegInit(false.B) 
    val mem_read_en = RegInit(false.B)
    val mem_write_en = RegInit(false.B)
    val cache_addr = RegInit(0.U(32.W))

    io.mem_read_en := mem_read_en
    io.mem_write_en := mem_write_en

    switch (next_state){
        is (s0){
            cache_read_en := false.B
            cache_write_en := false.B
            mem_read_en := false.B
            mem_write_en := false.B 
        }
        is (s1){
            cache_read_en := io.read_cache_en 
            cache_write_en := io.write_cache_en
            cache_addr := io.addr
            mem_read_en := false.B
            mem_write_en := false.B 
        }
        is (s2){
            cache_read_en := false.B
            cache_write_en := false.B 
            mem_read_en := false.B
            mem_write_en := true.B 
        }
        is (s3){
            cache_read_en := false.B
            cache_write_en := false.B
            mem_read_en := Mux(io.mem_read_fin,false.B,true.B)
            mem_write_en := false.B
        }
        is (s4){
            cache_read_en := io.read_cache_en 
            cache_write_en := io.write_cache_en
            mem_read_en := false.B
            mem_write_en := false.B 
        }
    }

    when (cur_state === s3 && io.mem_read_fin){
        substitude := true.B
    }.otherwise{
        substitude := false.B 
    }

    cache.io.addr := Mux(cache_curline_data_ready,cache_nextline_addr,io.addr) 
    cache.io.write_data := write_data
    cache.io.read_en := cache_read_en
    cache.io.write_en := cache_write_en
    cache.io.substitude := substitude  
    cache.io.substitude_data := substitude_data
    cache.io.substitude_index := cache_writeback_index
    substitude_fin :=  cache.io.substitude_fin
    dirty_bit := cache.io.dirty_bit 
    read_hit := cache.io.read_hit  
    read_miss := cache.io.read_miss 
    write_hit := cache.io.write_hit
    write_miss := cache.io.write_miss
    cache.io.write_mask := write_mask

}
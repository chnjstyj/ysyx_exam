import chisel3._
import chisel3.util._
import chisel3.experimental._
/*
采用状态机
空闲
比对     如果Hit 则转为空闲，如果miss，且dirty为1，转到写回，否则转到替换
写回     完成后转到替换
替换     完成后返回比对
*/
class cache_controller(
    tag_width:Int,
    index_width:Int,
    offset_width:Int,
    ways:Int 
) extends Module{
    val io = IO(new Bundle{
        val addr = Input(UInt(32.W))
        val read_cache_en = Input(Bool())
        val write_cache_en = Input(Bool()) 
        val mem_write_fin = Input(Bool())
        val mem_read_fin = Input(Bool())
        val cache_miss = Output(Bool())
        val cache_data = Output(UInt(64.W))
        val mem_read_en = Output(Bool())
        val mem_write_en = Output(Bool()) 
        val mem_read_data = Input(UInt((1 << (offset_width + 3)).W))
        val mem_addr = Output(UInt(32.W))
        val write_cache_data = Input(UInt(64.W))
        val cache_writeback_data = Output(UInt((1 << (offset_width + 3)).W))
        val read_cache_fin = Output(Bool())
        val write_cache_fin = Output(Bool())
    })

    val cache = Module(new cache(tag_width,index_width,offset_width,ways))

    val s0 :: s1 :: s2 :: s3 :: Nil = Enum(4)
    
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

    io.read_cache_fin := read_hit 
    io.write_cache_fin := write_hit 
    io.mem_addr := io.addr & (~((1 << (offset_width)) - 1).U(32.W))

    switch(cur_state){
        is (s0){
            when (io.read_cache_en || io.write_cache_en){
                next_state := s1
            }.otherwise{
                next_state := s0
            }
        }
        is (s1){
            when ((write_miss || read_miss) && dirty_bit){
                next_state := s2
            }.elsewhen (write_miss || read_miss){
                next_state := s3
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
                next_state := s1
            }.otherwise{
                next_state := s3
            }
        }
    }

    io.cache_data := cache.io.read_data 
    //when (next_state === s2 || next_state === s3 || next_state === s0){
    //    io.cache_miss := true.B
    //}.else
    when (next_state === s1 && read_hit === true.B){
        io.cache_miss := false.B
    }.otherwise{
        io.cache_miss := true.B
    }

    val cache_read_en  = RegInit(false.B)
    val cache_write_en = RegInit(false.B) 
    val mem_read_en = RegInit(false.B)
    val mem_write_en = RegInit(false.B)

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
            mem_read_en := true.B
            mem_write_en := false.B 
        }
    }

    when (cur_state === s3 && io.mem_read_fin){
        substitude := true.B
        //substitude_data := io.mem_read_data 
    }.otherwise{
        substitude := false.B
        //substitude_data := io.mem_read_data
    }

    cache.io.addr := io.addr 
    cache.io.write_data := io.write_cache_data 
    cache.io.read_en := cache_read_en 
    cache.io.write_en := cache_write_en
    cache.io.substitude := substitude  
    cache.io.substitude_data := substitude_data
    substitude_fin :=  cache.io.substitude_fin
    dirty_bit := cache.io.dirty_bit 
    read_hit := cache.io.read_hit  
    read_miss := cache.io.read_miss 
    write_hit := cache.io.write_hit
    write_miss := cache.io.write_miss
    io.cache_writeback_data := cache.io.writeback_data

}
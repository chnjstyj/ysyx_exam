import chisel3._
import chisel3.util._
import chisel3.experimental._

class cache_line(
    tag_width:Int,
    offset_width:Int
) extends Bundle{
    val valid = Bool() 
    val dirty = Bool()
    val tag = UInt(tag_width.W)
    //val data = Vec((1 << (offset_width)),UInt(8.W))
    val data = UInt((1 << (offset_width + 3)).W)
}

class cache(
    tag_width:Int,
    index_width:Int,
    offset_width:Int,
    ways:Int 
) extends Module {
    val io = IO(new Bundle{
        val addr = Input(UInt(32.W))
        val write_data = Input(UInt(64.W))
        val read_en = Input(Bool())
        val write_en = Input(Bool())
        val substitude = Input(Bool())
        val substitude_data = Input(UInt((1 << (offset_width + 3)).W))
        val substitude_fin = Output(Bool())
        val dirty_bit = Output(Bool())
        val read_hit = Output(Bool())
        val read_miss = Output(Bool())
        val write_hit = Output(Bool())
        val write_miss = Output(Bool()) 
        val writeback_data = Output(UInt((1 << (offset_width + 3)).W)) 
        val read_data = Output(UInt(64.W)) 
    })

    //define the cache
    val init_cache_line = Wire(new cache_line(tag_width,offset_width))
    init_cache_line.valid := false.B 
    init_cache_line.dirty := false.B 
    init_cache_line.tag := 0.U(tag_width.W)
    init_cache_line.data := 0.U((1 << (offset_width + 3)).W)
    //val cache_block = Vec((1 << index_width),new cache_line(tag_width,offset_width))
    //val cache_blocks = Reg(Vec(ways,cache_block))
    //cache_blocks := VecInit(Seq.fill(ways)(VecInit(Seq.fill(1 << index_width)(init_cache_line))))
    val cache_blocks = RegInit(VecInit(Seq.fill(ways)(VecInit(Seq.fill(1 << index_width)(init_cache_line)))))

    val cache_line_hitted = Wire(Vec(ways, new cache_line(tag_width, offset_width)))
    cache_line_hitted := VecInit((0 until ways).map(i => cache_blocks(i)(io.addr(index_width + offset_width - 1,offset_width))))
    //val cache_line_hitted = WireDefault(VecInit(Seq.fill(ways)((i: Int) => cache_blocks(i)(io.addr(index_width + offset_width - 1,offset_width)))))

    //0counters 
    val counters = RegInit(VecInit(Seq.fill(ways)(0.U(2.W))))
    val counter_min = RegInit(0.U(2.W))
    //counter_min := counters.reduce((x, y) => Mux(x < y, x, y))
    //(0 until ways).map(i => counter_min := Mux(counters(counter_min) <= counters(i),counter_min,i.asUInt))
    counters.zipWithIndex.foreach{case (counter,i) => {
        when (counter_min === 0.U){
            counter_min := i.asUInt
        }.otherwise{
            when (counters(counter_min) <= counter){
                
            }.otherwise{
                counter_min := i.asUInt
            }
        }
    }}
    val counter_equal = WireDefault(counters.forall(i => i === counters(0)))

    val addr_offset_3 = io.addr(offset_width-1,3)

    //write & substitude 
    val substitude_fin = RegInit(false.B)
    io.write_hit := false.B 
    io.write_miss := false.B 
    io.substitude_fin := substitude_fin
    when (io.write_en){
        io.write_hit := false.B
        io.write_miss := true.B
        /*
        cache_blocks.map(block => block.map(line => {
            when (line.valid && line.tag === io.addr(31,32 - tag_width)){
                val data_high = WireDefault(line.data >> ((addr_offset_3 << 6) + 64.U))
                val new_writedata = WireDefault(Cat(data_high,io.write_data))
                val mask = Wire(UInt(256.W))
                mask := (1.U(1.W) << (addr_offset_3 << 6)) - 1.U
                line.data := (line.data & (mask)) | (new_writedata << (addr_offset_3 << 6))
                line.dirty := true.B 
                io.write_hit := true.B 
                io.write_miss := false.B
            }
        }))*/
        cache_line_hitted.zipWithIndex.foreach{case (line,index) => {
            when (line.valid && line.tag === io.addr(31,32 - tag_width)){
                val data_high = WireDefault(line.data >> ((addr_offset_3 << 6) + 64.U))
                val new_writedata = WireDefault(Cat(data_high,io.write_data))
                val mask = Wire(UInt(256.W))
                mask := (1.U(1.W) << (addr_offset_3 << 6)) - 1.U
                cache_blocks(index)(io.addr(index_width + offset_width - 1,offset_width)).data := (line.data & (mask)) | (new_writedata << (addr_offset_3 << 6))
                cache_blocks(index)(io.addr(index_width + offset_width - 1,offset_width)).dirty := true.B 
                io.write_hit := true.B 
                io.write_miss := false.B
            }
        }}
    }.otherwise{
        when (io.substitude){
           when (counter_equal){
                //val newData = WireDefault(0.U((1 << (offset_width + 3)).W))
                //newData := io.substitude_data << (addr_offset_3 << 3)
                cache_blocks(0)(io.addr(index_width + offset_width - 1,offset_width)).valid := true.B 
                cache_blocks(0)(io.addr(index_width + offset_width - 1,offset_width)).dirty := false.B 
                cache_blocks(0)(io.addr(index_width + offset_width - 1,offset_width)).tag := io.addr(31,32 - tag_width)
                cache_blocks(0)(io.addr(index_width + offset_width - 1,offset_width)).data := io.substitude_data//cache_blocks(counters(0))(io.addr(index_width + offset_width - 1,offset_width)).data | newData
                substitude_fin := true.B 
           }.otherwise{
                val newData = WireDefault(0.U((1 << (offset_width + 3)).W))
                newData := io.substitude_data << (addr_offset_3 << 3)
                cache_blocks(counter_min)(io.addr(index_width + offset_width - 1,offset_width)).valid := true.B 
                cache_blocks(counter_min)(io.addr(index_width + offset_width - 1,offset_width)).dirty := false.B 
                cache_blocks(counter_min)(io.addr(index_width + offset_width - 1,offset_width)).tag := io.addr(31,32 - tag_width)
                cache_blocks(counter_min)(io.addr(index_width + offset_width - 1,offset_width)).data := io.substitude_data//cache_blocks(counter_min)(io.addr(index_width + offset_width - 1,offset_width)).data | newData
                substitude_fin := true.B 
           }
        }.otherwise{
            substitude_fin := false.B 
        }
    }

    //read 
    io.read_hit := false.B
    io.read_miss := true.B
    when (io.read_en){
        io.read_data := 0.U(64.W)
        cache_line_hitted.map(
            line => 
            when (line.valid && line.tag === io.addr(31,32 - tag_width)){
                io.read_data := (line.data >> (addr_offset_3 << (3+3)))(63,0)
                io.read_hit := true.B 
                io.read_miss := false.B
            }
        )
    }.otherwise{
        io.read_hit := false.B
        io.read_miss := false.B
        io.read_data := 0.U(64.W)
    }

    //substitude
    //full
    when (counter_equal){
        counters.foreach(counter => counter := 0.U)
    }
    //hit and add counter
    when (io.read_hit || io.write_hit){
        cache_line_hitted.zip(counters).zipWithIndex.foreach{case ((line,counter),index) => {
            when (line.valid && line.tag === io.addr(31,32 - tag_width)){
                when (counter =/= 3.U){
                    counter := counter + 1.U
                }.otherwise{
                    //val increased_counters = counters.tail.map(i => Mux(i === 0.U, 0.U, i - 1.U))
                    //counters := VecInit(Seq(counter) ++ increased_counters)
                    counter := counter + 1.U
                    counters.zipWithIndex.foreach{ case (i,x) => i := Mux(i === 0.U, 0.U, Mux((x == index).asBool,i, i - 1.U))}
                }
            }
        }}
        //after substituding, clean the counter
        when(io.substitude){
            when (counter_equal){
                counters(counters(0)) := 0.U
            }.otherwise{
                counters.zipWithIndex.foreach{case (counter,index) => {
                    when (counter === counter_min){
                        counters(index) := 0.U
                    }
                }}
            }
        }
    }

    //write back 
    when (counter_equal){
        io.dirty_bit := cache_line_hitted(counters(0)).dirty 
        //io.writeback_data := Cat(Seq(cache_line_hitted(counters(0)).data).reverse)
        //val dataSeq: Seq[chisel3.UInt] = cache_line_hitted(counters(0)).data.toSeq
        io.writeback_data := cache_line_hitted(counters(0)).data
    }.otherwise{
        io.dirty_bit := cache_line_hitted(counter_min).dirty 
        //io.writeback_data := Cat(Seq(cache_line_hitted(counter_min).data).reverse)
        //val dataSeq: Seq[chisel3.UInt] = cache_line_hitted(counters(0)).data.toSeq
        io.writeback_data := cache_line_hitted(counter_min).data
    }

}
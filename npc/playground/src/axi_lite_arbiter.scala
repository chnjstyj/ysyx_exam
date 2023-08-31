import chisel3._ 
import chisel3.util._ 

class axi_lite_arbiter extends Module {
    val io = IO(new Bundle{
        //ifu read
        val ifu_read_addr = Input(UInt(32.W))
        val ifu_read_en = Input(UInt(1.W)) 
        val ifu_read_valid = Output(UInt(1.W)) 
        val ifu_read_data = Output(UInt(64.W))
        //lsu read
        val lsu_read_addr = Input(UInt(32.W))
        val lsu_read_en = Input(UInt(1.W)) 
        val lsu_read_valid = Output(UInt(1.W)) 
        val lsu_read_data = Output(UInt(64.W))
        //lsu write
        val lsu_write_addr = Input(UInt(32.W))
        val lsu_write_data = Input(UInt(64.W))
        val lsu_write_en = Input(UInt(1.W)) 
        val lsu_write_finish = Output(UInt(1.W)) 
    })
    
    //s0 : idle
    //s1 : LSU
    //s2 : IFU
    //...
    val s0 :: s1 :: s2 :: Nil = Enum(3)

    val ifu_en = WireDefault(io.ifu_read_en)
    val lsu_en = WireDefault(io.lsu_read_en | io.lsu_write_en)

    val ifu_finish = WireDefault(io.ifu_read_valid)
    val lsu_finish = WireDefault(io.lsu_read_valid | io.lsu_write_finish)

    val next_state = WireDefault(s0)
    val cur_state = RegNext(next_state,s0) 

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

}
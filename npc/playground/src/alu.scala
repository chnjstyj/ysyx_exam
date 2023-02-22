import chisel3._
import chisel3.util._
import chisel3.experimental._

class alu(alu_control_width:Int) extends Module{
    val io = IO(new Bundle{
        val alu_control = Input(UInt(alu_control_width.W))
        val alu_src = Input(UInt(1.W))
        
        val rs1_rdata = Input(UInt(64.W))
        val rs2_rdata = Input(UInt(64.W)) 
        val imm = Input(UInt(64.W))

        val alu_result = Output(UInt(64.W))
    })

    //val result = WireDefault(0.U(64.W))

    val real_data_b = WireDefault(Mux(io.alu_src.asBool,io.imm,io.rs2_rdata))

    val add_result = WireDefault(io.rs1_rdata + real_data_b)
    val sub_result = WireDefault(io.rs1_rdata - real_data_b)
    val less_than_result = Wire(UInt(64.W))
    less_than_result := Cat(Fill(63,0.U(1.W)),sub_result(63))
    
    //result :=
    io.alu_result := "h0000_0000_0000_0000".U
    switch (io.alu_control){
        is ("b0".U){
            // +
            io.alu_result := add_result
        }
        is ("b1".U){
            // -
            io.alu_result := sub_result
        }
        is ("b10".U){
            //less than
            io.alu_result := less_than_result
        }
    }


}
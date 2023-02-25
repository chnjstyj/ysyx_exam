import chisel3._
import chisel3.util._
import chisel3.experimental._

package ALU{

class ALU_OPS{
    val ADD = "b0".U 
    val SUB = "b1".U 
    val LESS_THAN = "b10".U 
    val SRL = "b11".U 
    val AND = "b100".U

    val XOR = "b110".U 
    val SRA = "b111".U
}
}

class alu(alu_control_width:Int) extends Module{
    val io = IO(new Bundle{
        val alu_control = Input(UInt(alu_control_width.W))
        val alu_src = Input(UInt(1.W))
        val alu_result_size = Input(UInt(1.W))
        val sign_less_than = Input(UInt(1.W))
        
        val rs1_rdata = Input(UInt(64.W))
        val rs2_rdata = Input(UInt(64.W)) 
        val imm = Input(UInt(64.W))

        val alu_result = Output(UInt(64.W))
    })
    import ALU.ALU_OPS
    val alu_ops = new ALU_OPS

    val result = Wire(UInt(64.W))

    val real_data_b = WireDefault(Mux(io.alu_src.asBool,io.imm,io.rs2_rdata))

    val add_result = WireDefault(io.rs1_rdata + real_data_b)
    val sub_result = WireDefault(io.rs1_rdata - real_data_b)
    val less_than_result = Wire(UInt(64.W))
    less_than_result := Mux(io.sign_less_than.asBool,
        Cat(Fill(63,0.U(1.W)),io.rs1_rdata.asSInt < real_data_b.asSInt),
        Cat(Fill(63,0.U(1.W)),sub_result(63)))
    val sra_result = WireDefault((io.rs1_rdata.asSInt >> real_data_b(5,0)).asUInt)
    val xor_result = WireDefault(io.rs1_rdata ^ real_data_b)
    val and_result = WireDefault(io.rs1_rdata & real_data_b)
    
    io.alu_result := Mux(io.alu_result_size.asBool,Cat(Fill(32,result(31)),result(31,0)),result)
    /*
    when (io.alu_result_size === 1.U){
        io.alu_result := Cat(Fill(32,result(31)),result(31,0))
    }.otherwise{
        io.alu_result := result
    }*/
    result := MuxCase("h0000_0000_0000_0000".U,Seq(
        (io.alu_control === alu_ops.ADD) -> (add_result),
        (io.alu_control === alu_ops.SUB) -> (sub_result),
        (io.alu_control === alu_ops.LESS_THAN) -> (less_than_result),
        (io.alu_control === alu_ops.SRA) -> (sra_result),
        (io.alu_control === alu_ops.AND) -> (and_result),
        (io.alu_control === alu_ops.SRL) -> (add_result),
        (io.alu_control === alu_ops.XOR) -> (xor_result)
    ))
    /*
    result := "h0000_0000_0000_0000".U
    switch (io.alu_control){
        is ("b0".U){
            // +
            result := add_result
        }
        is ("b1".U){
            // -
            result := sub_result
        }
        is ("b10".U){
            //less than
            result := less_than_result
        }
        is ("b11".U){
            //arithmetic right shift
            result := sra_result
        }
        is ("b100".U){
            //logic right shift
        }
        is ("b101".U){
            //logic left shift
        }
        is ("b110".U){
            //xor
        }
    }*/


}
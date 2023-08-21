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
    val SLL = "b101".U
    val XOR = "b110".U 
    val SRA = "b111".U
    val OR  = "b1000".U
    val MUL = "b1001".U
    val DIV = "b1010".U
    val REM = "b1011".U
}

}

class alu(alu_control_width:Int) extends Module{
    val io = IO(new Bundle{
        val alu_control = Input(UInt(alu_control_width.W))
        val alu_src = Input(UInt(1.W))
        val alu_result_size = Input(UInt(1.W))
        val sign_less_than = Input(UInt(1.W))
        val sign_divrem = Input(UInt(1.W))
        val funct3 = Input(UInt(3.W))
        val csr_sen = Input(UInt(1.W))
        
        val rs1_rdata = Input(UInt(64.W))
        val rs2_rdata = Input(UInt(64.W)) 
        val csr_rdata = Input(UInt(64.W))
        val imm = Input(UInt(64.W))

        val alu_result = Output(UInt(64.W))
    })
    import ALU.ALU_OPS

    val alu_ops = new ALU_OPS
    val mul = Module(new mul)
    //calculate for signed number
    val rem = Module(new rem)
    val div = Module(new div)

    val result = Wire(UInt(64.W))

    val real_data_a_w = WireDefault(Mux(io.alu_result_size.asBool,
        //Cat(Fill(32,io.rs1_rdata(31)),io.rs1_rdata(31,0)),io.rs1_rdata))
    Cat(Fill(32,0.U(1.W)),io.rs1_rdata(31,0)),io.rs1_rdata))

    val real_data_b = WireDefault(Mux(io.alu_src.asBool,io.imm,
    Mux(io.csr_sen.asBool,io.csr_rdata,io.rs2_rdata)))

    val real_data_b_w = WireDefault(Mux(io.alu_result_size.asBool,
        //Cat(Fill(32,real_data_b(31)),real_data_b(31,0)),real_data_b))
    Cat(Fill(32,0.U(1.W)),real_data_b(31,0)),real_data_b))
    
    mul.io.funct3 := io.funct3
    mul.io.data_a := real_data_a_w
    mul.io.data_b := real_data_b_w

    rem.io.data_a := real_data_a_w
    rem.io.data_b := real_data_b_w

    div.io.data_a := real_data_a_w
    div.io.data_b := real_data_b_w

    val add_result = WireDefault(real_data_a_w + real_data_b_w)
    val sub_result = WireDefault(real_data_a_w - real_data_b_w)
    val less_than_result = Wire(UInt(64.W))
    less_than_result := Mux(io.sign_less_than.asBool,
        Cat(Fill(63,0.U(1.W)),real_data_a_w.asSInt < real_data_b_w.asSInt),
        Cat(Fill(63,0.U(1.W)),sub_result(63) ^ real_data_a_w(63)))

    val sra_result = Wire(UInt(64.W))
    sra_result := Mux(io.alu_result_size.asBool,
    Cat(Fill(32,real_data_a_w(31)),(real_data_a_w(31,0).asSInt >> real_data_b_w(5,0)).asUInt),
    (real_data_a_w.asSInt >> real_data_b_w(5,0)).asUInt)
    //WireDefault((real_data_a_w.asSInt >> real_data_b_w(5,0)).asUInt)
    //val srl_result = WireDefault(real_data_a_w.asUInt >> real_data_b_w(5,0))

    val srl_result = Wire(UInt(64.W))
    srl_result := Mux(io.alu_result_size.asBool,
    Cat(Fill(32,0.U),real_data_a_w(31,0) >> real_data_b_w(5,0)),
    real_data_a_w.asUInt >> real_data_b_w(5,0))

    val sll_result = WireDefault(real_data_a_w << real_data_b_w(5,0))
    val xor_result = WireDefault(real_data_a_w ^ real_data_b_w)
    val and_result = WireDefault(real_data_a_w & real_data_b_w)
    val or_result  = WireDefault(real_data_a_w | real_data_b_w)
    val mul_result = WireDefault(mul.io.result)

    val div_result = Wire(UInt(64.W))
    val divu_result = Wire(UInt(64.W))
    val divs_result = Wire(UInt(64.W))
    divu_result := real_data_a_w / real_data_b_w
    divs_result := div.io.result
    div_result := Mux(io.sign_divrem.asBool,
    (divs_result),
    (divu_result))

    val rem_result = Wire(UInt(64.W))
    val remu_result = Wire(UInt(64.W))
    val rems_result = Wire(UInt(64.W))
    remu_result := real_data_a_w % real_data_b_w
    rems_result := rem.io.result
    rem_result := Mux(io.sign_divrem.asBool,
    (rems_result),
    (remu_result))

    
    io.alu_result := Mux(io.alu_result_size.asBool,Cat(Fill(32,result(31)),result(31,0)),result)

    result := MuxCase("h0000_0000_0000_0000".U,Seq(
        (io.alu_control === alu_ops.ADD) -> (add_result),
        (io.alu_control === alu_ops.SUB) -> (sub_result),
        (io.alu_control === alu_ops.LESS_THAN) -> (less_than_result),
        (io.alu_control === alu_ops.SRA) -> (sra_result),
        (io.alu_control === alu_ops.AND) -> (and_result),
        (io.alu_control === alu_ops.SRL) -> (srl_result),
        (io.alu_control === alu_ops.XOR) -> (xor_result),
        (io.alu_control === alu_ops.SLL) -> (sll_result),
        (io.alu_control === alu_ops.OR ) -> (or_result ),
        (io.alu_control === alu_ops.MUL) -> (mul_result),
        (io.alu_control === alu_ops.DIV) -> (div_result),
        (io.alu_control === alu_ops.REM) -> (rem_result)
    ))
    
}
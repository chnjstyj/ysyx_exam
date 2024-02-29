import chisel3._
import chisel3.util._
import chisel3.experimental._
/*
class mul extends Module{
    val io = IO(new Bundle{
        val funct3 = Input(UInt(3.W))
        val data_a = Input(UInt(64.W))
        val data_b = Input(UInt(64.W))
        val result = Output(UInt(64.W))
    })

    val a = WireDefault(io.data_a)
    val b = WireDefault(io.data_b)
    
    val mul_result = WireDefault((a * b)(63,0)) 
    val mulh_result = WireDefault(((a.asSInt * b.asSInt)(127,64)).asUInt) 
    val mulhsu_result = WireDefault(((a.asSInt * b.asUInt)(127,64)).asUInt) 
    val mulhu_result = WireDefault(((a * b)(127,64)).asUInt) 

    io.result := MuxCase("h0000_0000_0000_0000".U,Seq(
        (io.funct3 === "b000".U) -> (mul_result),
        (io.funct3 === "b001".U) -> (mulh_result),
        (io.funct3 === "b010".U) -> (mulhsu_result),
        (io.funct3 === "b011".U) -> (mulhu_result)
    ))


}*/

class mul(
    xlen:Int
) extends Module{
    val io = IO(new Bundle{
        val mul_valid = Input(Bool())
        val flush = Input(Bool())
        val mulw = Input(Bool())
        val mul_signed = Input(UInt(2.W)) //2’b11（signed x signed）；2’b10（signed x unsigned）；2’b00（unsigned x unsigned）
        val multiplicand = Input(UInt(xlen.W))
        val multiplier = Input(UInt(xlen.W))
        val mul_ready = Output(Bool())
        val out_valid = Output(Bool())
        val result_hi = Output(UInt(xlen.W))
        val result_lo = Output(UInt(xlen.W))
    })

    val result = RegInit(0.U((2 * xlen).W)) 
    val multiplicand_s = Reg(UInt((2 * xlen).W)) //被乘数
    multiplicand_s := Cat(Fill(xlen,0.U),io.multiplicand) 
    val multiplier_s = Reg(UInt(xlen.W))
    multiplier_s := io.multiplier
    val counter = RegInit(0.U(log2Ceil(xlen).W))
    val counter_max = (xlen -1)

    io.result_hi := result >> xlen.U 
    io.result_lo := result(xlen-1,0) 
    io.mul_ready := counter === 0.U

    val running = RegInit(false.B)
    val below_zero = RegInit(false.B)
    val valid = RegInit(false.B)

    io.out_valid := valid
    
    when (io.flush){
        counter := 0.U 
        result := 0.U 
        running := false.B 
    }.elsewhen (io.mul_valid & !running){
        running := true.B 
        below_zero := io.multiplier(xlen - 1) && (io.mul_signed === "b11".U)
        result := 0.U
    }.elsewhen (running && counter =/= counter_max.U){
        when (multiplier_s(0) === 1.U){
            result := result + multiplicand_s 
        }
        multiplier_s := multiplier_s >> 1.U 
        multiplicand_s := multiplicand_s << 1.U 
        counter := counter + 1.U 
    }.elsewhen(running && counter === counter_max.U){
        when (multiplier_s(0) === 1.U){
            when (below_zero){
                result := (result.asSInt - multiplicand_s.asSInt).asUInt
            }.otherwise{
                result := result + multiplicand_s 
            }
        }
        //result := Mux(below_zero,(~result)+1.U,result)
        running := false.B
        valid := true.B
        counter := 0.U 
    }.otherwise{
        valid := false.B 
    }

}
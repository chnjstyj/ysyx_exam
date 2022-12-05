import chisel3._
import chisel3.util._
import chisel3.experimental._

class control_signal_bundle(alu_control_width:Int) extends Bundle{
    // 1 : imm; 0 : rs2
    val alu_src = Output(UInt(1.W))
    val alu_control = Output(UInt(alu_control_width.W))
    // 1 : write reg; 0 : not write reg
    val reg_wen = Output(UInt(1.W))
}

class id(alu_control_width:Int) extends Module{
    val io = IO(new Bundle{
        val inst = Input(UInt(32.W))
        val rs1 = Output(UInt(5.W))
        val rs2 = Output(UInt(5.W))
        val rd = Output(UInt(5.W))
        val imm = Output(UInt(64.W))

        val control_signal = new control_signal_bundle(alu_control_width)

    })

    val inst = WireDefault(io.inst)
    val imm_sign = WireDefault(inst(31,31))
    val imm_11_0 = WireDefault(inst(31,20))
    val imm_I = WireDefault(Cat(Fill(53,imm_sign),imm_11_0))
    /*
    val rs1 = WireDefault(inst(19,15))
    //val rs2 = WireDefault(io)
    val rd = WireDefault(inst(11,7))
    val imm_11_0 = WireDefault(inst(31,20))
    val funct3 = WireDefault(inst(14,12))
    val opcode = WireDefault(inst(6,0))
    */
    io.rs1 := inst(19,15)
    io.rs2 := inst(24,20)
    io.rd := inst(11,7)
    io.imm := 0.U(64.W)

    io.control_signal.alu_src := 0.U
    io.control_signal.alu_control := 0.U
    io.control_signal.reg_wen := 0.U

    val funct3 = WireDefault(inst(14,12))
    val opcode = WireDefault(inst(6,0))

    switch (opcode){
        is ("b0010011".U){  
            //addi slti sltiu xori ori andi slli srli srai
            switch (funct3){
                is ("b000".U){
                    //addi 
                    io.control_signal.alu_control := "b0".U
                    io.control_signal.reg_wen := 1.U
                    io.control_signal.alu_src := 1.U
                    
                    io.imm := imm_I
                }
            }
        }
    }

}
import chisel3._
import chisel3.util._
import chisel3.experimental._

class control_signal_bundle(alu_control_width:Int) extends Bundle{
    // 1 : imm; 0 : rs2
    val alu_src = Output(UInt(1.W))
    val alu_control = Output(UInt(alu_control_width.W))
    // 1 : write reg; 0 : not write reg
    val reg_wen = Output(UInt(1.W))
    // 11 : pc; 
    // 01 : zero; 
    // 00 : default
    val regfile_output_1 = Output(UInt(2.W))
    // 1 : jump; 0 : not
    val direct_jump = Output(UInt(1.W))
    // 1 : save; 0 : not
    val save_next_inst_addr = Output(UInt(1.W))
    // 1 : exit; 0 : not exit
    val exit_debugging = Output(UInt(1.W))
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
    val imm_sign = WireDefault(inst(31))
    val imm_31_20 = WireDefault(inst(31,20))
    val imm_20 = WireDefault(inst(20))
    val imm_19_12 = WireDefault(inst(19,12))

    val imm_J = WireDefault(Cat(Fill(43,imm_sign),
        Cat(imm_sign,
            Cat(imm_19_12,
                Cat(imm_20,
                    Cat(imm_31_20(10,1),0.U))))))
    val imm_I = WireDefault(Cat(Fill(53,imm_sign),imm_31_20))
    val imm_U = WireDefault(Cat(Fill(32,imm_sign),
        Cat(imm_31_20,
            Cat(imm_19_12,Fill(12,0.U)))))
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
    io.control_signal.exit_debugging := 0.U
    io.control_signal.regfile_output_1 := 0.U
    io.control_signal.direct_jump := 0.U
    io.control_signal.save_next_inst_addr := 0.U

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
        is ("b1110011".U){
            //ebreak
            switch (imm_31_20){
                is ("b000000000001".U){
                    io.control_signal.exit_debugging := 1.U
                }
            }
        }
        is ("b0110111".U){
            //lui
            io.control_signal.alu_control := "b0".U
            io.control_signal.reg_wen := 1.U 
            io.control_signal.alu_src := 1.U
            io.control_signal.regfile_output_1 := 1.U

            io.imm := imm_U
        }
        is ("b0010111".U){
            //auipc
            io.control_signal.alu_control := "b0".U
            io.control_signal.reg_wen := 1.U 
            io.control_signal.alu_src := 1.U
            io.control_signal.regfile_output_1 := 3.U

            io.imm := imm_U
        }
        is ("b1101111".U){
            //jal
            io.control_signal.direct_jump := 1.U
            io.control_signal.alu_control := "b0".U
            io.control_signal.alu_src := 1.U 
            io.control_signal.regfile_output_1 := 3.U 
            io.control_signal.save_next_inst_addr := 1.U

            io.imm := imm_J
        }
        is ("b1100111".U){
            //jalr
            io.control_signal.direct_jump := 1.U 
            io.control_signal.alu_control := "b0".U 
            io.control_signal.alu_src := 1.U 
            io.control_signal.save_next_inst_addr := 1.U 

            io.imm := imm_I
        }
    }

}
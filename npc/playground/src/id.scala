import chisel3._
import chisel3.util._
import chisel3.experimental._
import ALU.ALU_OPS

class control_signal_bundle(alu_control_width:Int) extends Bundle{
    //rs2 1 : imm; 0 : rs2
    val alu_src = Output(UInt(1.W))
    //ops
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
    // 1 : write to mem; 0 : not
    val mem_write_en = Output(UInt(1.W))
    // b1000 : 8 bytes; 
    // b0100 : 4 bytes; so on;
    val mem_wmask = Output(UInt(4.W))
    // 1 : judge branch; 0 : not
    val judge_branch = Output(UInt(1.W))
    // 1 : memory load; 0 : not 
    val mem_read_en = Output(UInt(1.W))
    // b1000 : 8 bytes
    // ...
    val mem_read_size = Output(UInt(4.W))
    // 1 : 32bits result; 0 : default
    val alu_result_size = Output(UInt(1.W))
    // 1 : zero extends; 0 : sign extends
    val zero_extends = Output(UInt(1.W))
    // 1 : SInt less than; 0 : UInt less than
    val sign_less_than = Output(UInt(1.W))
    // 1 : sign div/rem; 0 : unsign
    val sign_divrem = Output(UInt(1.W))
    // 1 : csr write; 0 : not , addr = immI
    val csr_wen = Output(UInt(1.W))
    // 1 : csr set; 0 : not
    val csr_sen = Output(UInt(1.W))
    // 1 : ecall; 0 : not
    val ecall = Output(UInt(1.W))
    // 1 : csr write to reg; 0 : not
    val csr_write_to_reg = Output(UInt(1.W))
    // 1 : mret; 0 : not
    val mret = Output(UInt(1.W))
}

class id(alu_control_width:Int) extends Module{
    val io = IO(new Bundle{
        val inst = Input(UInt(32.W))
        val rs1 = Output(UInt(5.W))
        val rs2 = Output(UInt(5.W))
        val rd = Output(UInt(5.W))
        val imm = Output(UInt(64.W))
        //to judge_branch
        val funct3 = Output(UInt(3.W))

        val control_signal = new control_signal_bundle(alu_control_width)

    })

    val alu_ops = new ALU_OPS

    val inst = WireDefault(io.inst)
    val imm_sign = WireDefault(inst(31))
    val imm_31_20 = WireDefault(inst(31,20))
    val imm_20 = WireDefault(inst(20))
    val imm_19_12 = WireDefault(inst(19,12))

    val funct3 = WireDefault(inst(14,12))
    val opcode = WireDefault(inst(6,0))
    val funct7 = WireDefault(inst(31,25))

    val imm_J = WireDefault(Cat(Fill(43,imm_sign),
        Cat(imm_sign,
            Cat(imm_19_12,
                Cat(imm_20,
                    Cat(imm_31_20(10,1),0.U))))))
    val imm_I = WireDefault(Cat(Fill(52,imm_sign),imm_31_20))
    val imm_U = WireDefault(Cat(Fill(32,imm_sign),
        Cat(imm_31_20,
            Cat(imm_19_12,Fill(12,0.U)))))
    val imm_S = WireDefault(Cat(Fill(52,imm_sign),funct7,io.rd))
    val imm_B = WireDefault(Cat(Fill(52,imm_sign),inst(7),inst(30,25),inst(11,8),0.U(1.W)))

    io.rs1 := inst(19,15)
    io.rs2 := inst(24,20)
    io.rd := inst(11,7)
    io.imm := 0.U(64.W)
    io.funct3 := funct3

    //default settings
    io.control_signal.alu_src := 0.U
    io.control_signal.alu_control := alu_ops.ADD
    io.control_signal.reg_wen := 0.U
    io.control_signal.exit_debugging := 0.U
    io.control_signal.regfile_output_1 := 0.U
    io.control_signal.direct_jump := 0.U
    io.control_signal.save_next_inst_addr := 0.U
    io.control_signal.mem_write_en := 0.U
    io.control_signal.mem_wmask := 0.U
    io.control_signal.judge_branch := 0.U
    io.control_signal.mem_read_en := 0.U 
    io.control_signal.mem_read_size := "b1000".U
    io.control_signal.alu_result_size := 0.U
    io.control_signal.zero_extends := 0.U
    io.control_signal.sign_less_than := 0.U
    io.control_signal.sign_divrem := 0.U
    io.control_signal.csr_wen := 0.U
    io.control_signal.csr_sen := 0.U
    io.control_signal.ecall := 0.U
    io.control_signal.csr_write_to_reg := 0.U
    io.control_signal.mret := 0.U

    switch (opcode){
        is ("b0010011".U){  
            //addi slti sltiu xori ori andi slli srli srai
            io.control_signal.reg_wen := 1.U
            io.control_signal.alu_src := 1.U

            io.imm := imm_I
            switch (funct3){
                is ("b000".U){
                    //addi 
                    io.control_signal.alu_control := alu_ops.ADD
                }
                is ("b010".U){
                    //slti
                    io.control_signal.alu_control := alu_ops.LESS_THAN
                    io.control_signal.sign_less_than := 1.U
                }
                is ("b011".U){
                    //sltiu
                    io.control_signal.alu_control := alu_ops.LESS_THAN
                }
                is ("b101".U){
                    //srli srai
                    //0000 0100
                    io.control_signal.alu_control := alu_ops.SRL | imm_I(10,8)
                }  
                is ("b100".U){
                    //xori
                    io.control_signal.alu_control := alu_ops.XOR
                }          
                is ("b111".U){
                    //andi
                    io.control_signal.alu_control := alu_ops.AND
                }
                is ("b001".U){
                    //slli
                    io.control_signal.alu_control := alu_ops.SLL
                }
            }
        }
        is ("b0011011".U){
            //ADDIW SLLIW SRLIW SRAIW
            io.control_signal.alu_result_size := 1.U
            io.control_signal.alu_src := 1.U 
            io.control_signal.reg_wen := 1.U 

            io.imm := imm_I
            switch (funct3){
                is ("b000".U){
                    //addiw 
                    io.control_signal.alu_control := alu_ops.ADD
                }
                is ("b001".U){
                    //slliw 
                    io.control_signal.alu_control := alu_ops.SLL 
                }
                is ("b101".U){
                    //sraiw srliw
                    io.control_signal.alu_control := alu_ops.SRL | funct7(5,3)
                }
            }
        }
        is ("b0111011".U){
            //ADDW SUBW SLLW SRLW SRAW
            //MULW DIVW DIVUW REMW REMUW
            io.control_signal.alu_result_size := 1.U
            io.control_signal.reg_wen := 1.U
            when (funct7 =/= "b0000001".U){
                switch (funct3){
                    is ("b000".U){
                        //addw subw 
                        //00   01 
                        io.control_signal.alu_control := alu_ops.ADD | funct7(5)
                    }
                    is ("b001".U){
                        //sllw
                        io.control_signal.alu_control := alu_ops.SLL
                    }
                    is ("b101".U){
                        //srlw sraw
                        // 00   01
                        io.control_signal.alu_control := alu_ops.SRL | funct7(5,3)
                    }
                }
            }.otherwise{            
                switch (funct3){
                    is ("b000".U){
                        //mulw
                        io.control_signal.alu_control := alu_ops.MUL
                    }
                    is ("b100".U){
                        //divw
                        io.control_signal.alu_control := alu_ops.DIV
                        io.control_signal.sign_divrem := 1.U 
                    }
                    is ("b101".U){
                        //divuw 
                        io.control_signal.alu_control := alu_ops.DIV
                    }
                    is ("b110".U){
                        //remw 
                        io.control_signal.sign_divrem := 1.U 
                        io.control_signal.alu_control := alu_ops.REM
                    }
                    is ("b111".U){
                        //remuw 
                        io.control_signal.alu_control := alu_ops.REM
                    }
                }
            }
        }
        is ("b0110011".U){
            //add sub sll slt sltu xor srl sra or and
            //MUL MULH MULHSU MULHU DIV DIVU REM REMU
            io.control_signal.reg_wen := 1.U
            when (funct7 =/= "b0000001".U){
                switch (funct3){
                    is ("b000".U){
                        //add sub 
                        //00  01
                        io.control_signal.alu_control := alu_ops.ADD | funct7(5)
                    }
                    is ("b001".U){
                        //sll
                        io.control_signal.alu_control := alu_ops.SLL 
                    }
                    is ("b011".U){
                        //sltu
                        io.control_signal.alu_control := alu_ops.LESS_THAN
                    }
                    is ("b111".U){
                        //and 
                        io.control_signal.alu_control := alu_ops.AND
                    }
                    is ("b110".U){
                        //or 
                        io.control_signal.alu_control := alu_ops.OR
                    }
                    is ("b100".U){
                        //xor
                        io.control_signal.alu_control := alu_ops.XOR 
                    }
                    is ("b101".U){
                        //srl sra
                        //011 111
                        io.control_signal.alu_control := alu_ops.SRL | funct7(5,3)
                    }
                    is ("b010".U){
                        //slt
                        io.control_signal.sign_less_than := 1.U 
                        io.control_signal.alu_control := alu_ops.LESS_THAN
                    }
                }
            }.otherwise{
                //mul ..
                io.control_signal.alu_control := alu_ops.MUL
                switch (funct3){
                    is ("b100".U){
                        //div 
                        io.control_signal.alu_control := alu_ops.DIV
                        io.control_signal.sign_divrem := 1.U
                    }
                    is ("b101".U){
                        //divu 
                        io.control_signal.alu_control := alu_ops.DIV
                        io.control_signal.sign_divrem := 0.U
                    }
                    is ("b110".U){
                        //rem 
                        io.control_signal.alu_control := alu_ops.REM
                        io.control_signal.sign_divrem := 1.U
                    }
                    is ("b111".U){
                        //remu 
                        io.control_signal.alu_control := alu_ops.REM
                        io.control_signal.sign_divrem := 0.U
                    }
                }

            }
        }
        is ("b1110011".U){
            //ebreak CSRRW CSRRS CSRRC CSRRWI CSRRSI CSRRCI
            switch (funct3){
                is ("b000".U){
                    switch (imm_31_20){
                        //ebreak
                        is ("b000000000001".U){
                            io.control_signal.exit_debugging := 1.U
                        }
                        //ecall
                        is ("b000000000000".U){
                            io.control_signal.ecall := 1.U
                        }
                        //mret
                        is ("b001100000010".U){
                            io.control_signal.mret := 1.U
                        }
                    }
                }
                is ("b001".U){
                    //CSRRW
                    io.control_signal.csr_wen := 1.U 
                    io.imm := imm_I
                    io.control_signal.csr_write_to_reg := 1.U
                    io.control_signal.alu_control := alu_ops.NONE
                }
                is ("b010".U){
                    //CSRRS
                    io.control_signal.csr_sen := (io.rs1 =/= 0.U)
                    io.imm := imm_I
                    io.control_signal.csr_write_to_reg := 1.U
                    io.control_signal.alu_control := alu_ops.OR
                }
            }
        }
        is ("b0110111".U){
            //lui
            io.control_signal.alu_control := alu_ops.ADD
            io.control_signal.reg_wen := 1.U 
            io.control_signal.alu_src := 1.U
            io.control_signal.regfile_output_1 := 1.U

            io.imm := imm_U
        }
        is ("b0010111".U){
            //auipc
            io.control_signal.alu_control := alu_ops.ADD
            io.control_signal.reg_wen := 1.U 
            io.control_signal.alu_src := 1.U
            io.control_signal.regfile_output_1 := 3.U

            io.imm := imm_U
        }
        is ("b1100011".U){
            // BEQ BNE BLT BGE BLTU BGEU
            // setting in judge_branch.scala
            io.control_signal.judge_branch := 1.U
            io.control_signal.regfile_output_1 := 0.U

            io.imm := imm_B
        }
        is ("b1101111".U){
            //jal
            io.control_signal.direct_jump := 1.U
            io.control_signal.alu_control := alu_ops.ADD
            io.control_signal.alu_src := 1.U 
            io.control_signal.regfile_output_1 := 3.U 
            io.control_signal.save_next_inst_addr := 1.U

            io.imm := imm_J
        }
        is ("b1100111".U){
            //jalr
            io.control_signal.direct_jump := 1.U 
            io.control_signal.alu_control := alu_ops.ADD
            io.control_signal.alu_src := 1.U 
            io.control_signal.save_next_inst_addr := 1.U 

            io.imm := imm_I
        }
        is ("b0100011".U){
            //store
            io.control_signal.mem_write_en := 1.U
            io.control_signal.alu_src := 1.U
            io.control_signal.alu_control := alu_ops.ADD

            io.imm := imm_S
            switch (funct3){
                is ("b011".U){
                    //sd
                    io.control_signal.mem_wmask := "b1000".U
                }
                is ("b010".U){
                    //sw
                    io.control_signal.mem_wmask := "b0100".U
                }
                is ("b001".U){
                    //sh
                    io.control_signal.mem_wmask := "b0010".U
                }
                is ("b000".U){
                    //sb
                    io.control_signal.mem_wmask := "b0001".U
                }
            }
        }
        is ("b0000011".U){
            //load 
            io.control_signal.mem_read_en := 1.U
            io.control_signal.alu_src := 1.U
            io.control_signal.alu_control := alu_ops.ADD
            io.imm := imm_I
            switch (funct3){
                is ("b011".U){
                    //ld
                    io.control_signal.mem_read_size := "b1000".U
                }
                is ("b010".U){
                    //lw 
                    io.control_signal.mem_read_size := "b0100".U 
                }
                is ("b110".U){
                    //lwu
                    io.control_signal.mem_read_size := "b0100".U 
                    io.control_signal.zero_extends := 1.U
                }
                is ("b001".U){
                    //lh 
                    io.control_signal.mem_read_size := "b0010".U
                }
                is ("b101".U){
                    //lhu
                    io.control_signal.mem_read_size := "b0010".U
                    io.control_signal.zero_extends := 1.U
                }
                is ("b000".U){
                    //lb
                    io.control_signal.mem_read_size := "b0001".U
                }
                is ("b100".U){
                    //lbu
                    io.control_signal.mem_read_size := "b0001".U
                    io.control_signal.zero_extends := 1.U
                }
            }
        }
    }

}
import chisel3._
import chisel3.util._
import chisel3.experimental._

class regfile extends Module{
    val io = IO(new Bundle{
        val rs1 = Input(UInt(5.W))
        val rs2 = Input(UInt(5.W))
        val rd = Input(UInt(5.W))
        val rd_wdata = Input(UInt(64.W))
        val next_inst_address = Input(UInt(64.W))

        val inst_address = Input(UInt(64.W))
        
        val save_next_inst_addr = Input(UInt(1.W))
        val reg_wen = Input(UInt(1.W))
        val regfile_output_1 = Input(UInt(2.W))

        val rs1_rdata = Output(UInt(64.W))
        val rs2_rdata = Output(UInt(64.W))

    })

    //val regfile = RegInit(RegInit(VecInit(Seq.fill(31)(0.U(64.W)))))
    val regs= Module(new regs)
    regs.io.clock := clock 
    regs.io.rs1 := io.rs1 
    regs.io.rs2 := io.rs2 
    regs.io.rd := io.rd 
    regs.io.reg_wen := io.reg_wen

    //read
    when (io.regfile_output_1 === 1.U){
        io.rs1_rdata := 0.U(64.W)
    }.elsewhen (io.regfile_output_1 === 3.U){
        io.rs1_rdata := io.inst_address
    }.otherwise{
        //io.rs1_rdata := regfile(io.rs1)
        io.rs1_rdata := regs.io.rs1_rdata
    }


    //io.rs2_rdata := regfile(io.rs2)
    io.rs2_rdata := regs.io.rs2_rdata

    //write
    when (io.reg_wen === 1.U){
        when (io.rd =/= 0.U){
            regs.io.rd_wdata := io.rd_wdata
        }
    }.elsewhen (io.save_next_inst_addr === 1.U){
        when (io.rd =/= 0.U){
            regs.io.rd_wdata := io.next_inst_address
        }
    }

}
import chisel3._
import chisel3.util._
import chisel3.experimental._

class regfile extends Module{
    val io = IO(new Bundle{
        val rs1 = Input(UInt(5.W))
        val rs2 = Input(UInt(5.W))
        val csr_addr = Input(UInt(12.W))
        val rd = Input(UInt(5.W))
        val rd_wdata = Input(UInt(64.W))

        val inst_address = Input(UInt(64.W))
        
        val save_next_inst_addr = Input(UInt(1.W))
        val reg_wen = Input(UInt(1.W))
        val mem_read_en = Input(UInt(1.W))
        val regfile_output_1 = Input(UInt(2.W))
        val csr_wen = Input(UInt(1.W))
        val csr_sen = Input(UInt(1.W))
        val ecall = Input(UInt(1.W))
        val csr_write_to_reg = Input(UInt(1.W))

        val rs1_rdata = Output(UInt(64.W))
        val rs2_rdata = Output(UInt(64.W))
        val csr_rdata = Output(UInt(64.W))
        val mret_addr = Output(UInt(64.W))

        val stall_ca_wb = Input(Bool())
        val stall_id_ex = Input(Bool())
    })

    val csr_write_to_reg = WireDefault(io.rd =/= 0.U && io.csr_write_to_reg === 1.U)
    val csr_rdata = Wire(UInt(64.W))

    //val regfile = RegInit(RegInit(VecInit(Seq.fill(31)(0.U(64.W)))))
    val regs = Module(new regs)
    val csrs = Module(new csrs)

    io.mret_addr := csrs.io.mret_addr

    regs.io.clock := clock 
    regs.io.rs1 := io.rs1 
    regs.io.rs2 := io.rs2
    regs.io.rd := io.rd 
    regs.io.reg_wen := ((io.reg_wen | io.mem_read_en | io.save_next_inst_addr)) | (!io.stall_id_ex & (io.csr_write_to_reg))

    csrs.io.clock := clock
    csrs.io.rs1_rdata := regs.io.rs1_rdata
    csrs.io.rd_wdata := io.rd_wdata
    csrs.io.csr_wen := !io.stall_id_ex & io.csr_wen
    csrs.io.csr_sen := io.csr_sen
    csrs.io.csr_addr := io.csr_addr
    csrs.io.ecall := io.ecall
    csrs.io.ecall_idx := regs.io.ecall_idx
    csrs.io.pc := io.inst_address
    csr_rdata := csrs.io.csr_rdata
    io.csr_rdata := csr_rdata

    //read
    when (io.regfile_output_1 === 1.U){
        io.rs1_rdata := 0.U(64.W)
    }.elsewhen (io.regfile_output_1 === 3.U){
        io.rs1_rdata := io.inst_address
    }.otherwise{
        //io.rs1_rdata := regfile(io.rs1)
        when (io.rs1 === io.rd && io.rs1 =/= 0.U && (io.reg_wen.asBool | io.mem_read_en.asBool | io.save_next_inst_addr.asBool)){
            io.rs1_rdata := io.rd_wdata
        }.otherwise{
            io.rs1_rdata := regs.io.rs1_rdata
        }
    }


    //io.rs2_rdata := regfile(io.rs2)
    when (io.rs2 === io.rd && io.rs2 =/= 0.U && (io.reg_wen.asBool | io.mem_read_en.asBool | io.save_next_inst_addr.asBool)){
        io.rs2_rdata := io.rd_wdata
    }.otherwise{
        io.rs2_rdata := regs.io.rs2_rdata
    }

    //write
    when (io.rd =/= 0.U && csr_write_to_reg =/= 1.U){
        regs.io.rd_wdata := io.rd_wdata
    }.elsewhen (io.rd =/= 0.U && csr_write_to_reg === 1.U){
        regs.io.rd_wdata := csr_rdata
    }

}
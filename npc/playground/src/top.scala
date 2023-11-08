import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile
import chisel3.util.experimental.loadMemoryFromFileInline

class top(
    tag_width:Int,
    index_width:Int,
    offset_width:Int,
    ways:Int 
) extends Module{
    val io = IO(new Bundle{
        val inst = Output(UInt(32.W))
        val inst_address = Output(UInt(64.W))
        val next_inst_address = Output(UInt(64.W))
        val stall = Output(Bool())
        val offset1 = Output(UInt(6.W))
    })

    val alu_control_width = 4

    val pc = Module(new pc) 
    val axi_lite_arbiter = Module(new axi_lite_arbiter(offset_width))
    val inst_if = Module(new inst_if("inst.rom")) 
    val id = Module(new id(alu_control_width))
    val regfile = Module(new regfile)
    val alu = Module(new alu(alu_control_width))
    val stall = Module(new stall)
    val mem = Module(new mem)
    val judge_branch_m = Module(new judge_branch_m)
    val icache_controller = Module(new cache_controller(tag_width,index_width,offset_width,ways))
    val dcache_controller = Module(new cache_controller(tag_width,index_width,offset_width,ways))

    val pc_inst_if = Module(new pc_inst_if)
    val inst_if_id = Module(new inst_if_id)
    val id_ex = Module(new id_ex(alu_control_width))
    val ex_mem = Module(new ex_mem)
    val mem_ca = Module(new mem_ca) 
    val ca_wb = Module(new ca_wb)

    io.inst := inst_if.io.inst
    when (pc.io.direct_jump === 1.U){
        io.inst_address := pc.io.inst_address | "h8000_0000".U 
    }.otherwise{
        io.inst_address := pc.io.inst_address | "h8000_0000".U  
    }
    io.next_inst_address := pc_inst_if.io.inst_if_next_inst_address
    io.stall := stall.io.stall_global//inst_if.io.stall_from_inst_if | mem.io.stall_from_mem

    //withClock((!clock.asBool).asClock){
        //val direct_jump_r = RegNext( id.io.control_signal.direct_jump )
        //val direct_jump_addr_r = RegNext( alu.io.alu_result )
        //val branch_jump_r = RegNext( judge_branch_m.io.branch_jump )
        //val branch_jump_addr_r = RegNext( judge_branch_m.io.branch_jump_addr )
        //FIXME direct jump addr  judge branch
        pc.io.direct_jump := id.io.control_signal.direct_jump
        pc.io.direct_jump_addr := alu.io.alu_result
        pc.io.branch_jump := judge_branch_m.io.branch_jump
        pc.io.branch_jump_addr := judge_branch_m.io.branch_jump_addr 
    //}

    pc.io.ecall := id.io.control_signal.ecall
    pc.io.ecall_addr := regfile.io.csr_rdata
    pc.io.mret := id.io.control_signal.mret
    pc.io.mret_addr := regfile.io.mret_addr
    pc.io.stall_global := stall.io.stall_global

    pc_inst_if.io.pc_inst_address := pc.io.inst_address
    pc_inst_if.io.pc_next_inst_address := pc.io.next_inst_address   
    pc_inst_if.io.pc_ce := pc.io.ce

    inst_if.io.ACLK := clock
    inst_if.io.ARESETn := ~(reset.asBool)
    inst_if.io.inst_address := pc_inst_if.io.inst_if_inst_address
    inst_if.io.ce := pc_inst_if.io.inst_if_ce
    inst_if.io.stall_global := stall.io.stall_global
    inst_if.io.stall_from_mem_reg := RegNext(stall.io.stall_from_mem_reg)
    //inst_if.io.ifu_read_data := axi_lite_arbiter.io.ifu_read_data
    //inst_if.io.ifu_read_valid := axi_lite_arbiter.io.ifu_read_valid
    inst_if.io.icache_read_data := icache_controller.io.cache_data 
    inst_if.io.icache_read_valid := axi_lite_arbiter.io.ifu_read_valid

    icache_controller.io.addr := inst_if.io.icache_read_addr 
    icache_controller.io.read_cache_en := inst_if.io.icache_read_en 
    icache_controller.io.write_cache_en := false.B 
    icache_controller.io.mem_write_fin := false.B 
    icache_controller.io.mem_read_fin := axi_lite_arbiter.io.ifu_read_valid
    icache_controller.io.mem_read_data := axi_lite_arbiter.io.ifu_read_data
    icache_controller.io.write_cache_data := 0.U(64.W) 
    icache_controller.io.write_cache_mask := 0.U(4.W)
    icache_controller.io.read_cache_size := "b0100".U

    inst_if_id.io.inst_if_inst := inst_if.io.inst
    inst_if_id.io.inst_if_inst_address := pc_inst_if.io.inst_if_inst_address
    inst_if_id.io.inst_if_next_inst_address := pc_inst_if.io.inst_if_next_inst_address

    id.io.inst := inst_if_id.io.id_inst 

    judge_branch_m.io.judge_branch := id.io.control_signal.judge_branch
    judge_branch_m.io.imm := id.io.imm 
    judge_branch_m.io.rs1_rdata := regfile.io.rs1_rdata
    judge_branch_m.io.rs2_rdata := regfile.io.rs2_rdata
    judge_branch_m.io.inst_address := pc.io.inst_address
    judge_branch_m.io.funct3 := id.io.funct3

    regfile.io.rs1 := id.io.rs1 
    regfile.io.rs2 := id.io.rs2 
    regfile.io.rd := ca_wb.io.wb_rd 
    regfile.io.reg_wen := ca_wb.io.wb_reg_wen
    regfile.io.regfile_output_1 := id.io.control_signal.regfile_output_1
    regfile.io.inst_address := inst_if_id.io.id_inst_address
    //FIXME save_next_inst_addr & next_instaddress
    regfile.io.save_next_inst_addr := id.io.control_signal.save_next_inst_addr
    regfile.io.mem_read_en := ca_wb.io.ca_mem_read_en
    regfile.io.rd_wdata := MuxCase(ca_wb.io.wb_alu_result,Seq(
        id.io.control_signal.save_next_inst_addr.asBool -> inst_if_id.io.id_next_inst_address,
        ca_wb.io.ca_mem_read_en.asBool -> ca_wb.io.wb_mem_read_data))
    regfile.io.csr_wen := id.io.control_signal.csr_wen
    //FIXME from wb 
    regfile.io.csr_sen := ca_wb.io.wb_csr_sen
    regfile.io.csr_addr := ca_wb.io.wb_csr_addr
    regfile.io.ecall := id.io.control_signal.ecall
    regfile.io.csr_write_to_reg := id.io.control_signal.csr_write_to_reg
    regfile.io.stall_global := stall.io.stall_global
    /*
    when (id.io.control_signal.save_next_inst_addr === 1.U){
        regfile.io.rd_wdata := pc.io.next_inst_address
    }.elsewhen (id.io.control_signal.mem_read_en === 1.U){
        regfile.io.rd_wdata := mem.io.mem_read_data
    }.otherwise{
        regfile.io.rd_wdata := alu.io.alu_result
    }*/

    id_ex.io.id_alu_src := id.io.control_signal.alu_src
    id_ex.io.id_alu_control := id.io.control_signal.alu_control
    id_ex.io.id_reg_wen := id.io.control_signal.reg_wen
    id_ex.io.id_rd := id.io.rd
    id_ex.io.id_mem_write_en := id.io.control_signal.mem_write_en
    id_ex.io.id_mem_write_wmask := id.io.control_signal.mem_wmask
    id_ex.io.id_mem_read_en := id.io.control_signal.mem_read_en
    id_ex.io.id_mem_read_size := id.io.control_signal.mem_read_size
    id_ex.io.id_alu_result_size := id.io.control_signal.alu_result_size
    id_ex.io.id_zero_extends := id.io.control_signal.zero_extends
    id_ex.io.id_funct3 := id.io.funct3
    id_ex.io.id_rs1_rdata := regfile.io.rs1_rdata
    id_ex.io.id_rs2_rdata := regfile.io.rs2_rdata
    id_ex.io.id_csr_rdata := regfile.io.csr_rdata
    id_ex.io.id_imm := id.io.imm
    id_ex.io.id_sign_less_than := id.io.control_signal.sign_less_than
    id_ex.io.id_csr_sen := id.io.control_signal.csr_sen
    id_ex.io.id_csr_addr := id.io.imm
    id_ex.io.id_sign_divrem := id.io.control_signal.sign_divrem

    alu.io.alu_control := id_ex.io.ex_alu_control
    alu.io.alu_src := id_ex.io.ex_alu_src
    alu.io.rs1_rdata := id_ex.io.ex_rs1_rdata
    alu.io.rs2_rdata := id_ex.io.ex_rs2_rdata
    alu.io.imm := id_ex.io.ex_imm
    alu.io.alu_result_size := id_ex.io.ex_alu_result_size
    alu.io.sign_less_than := id_ex.io.ex_sign_less_than
    alu.io.sign_divrem := id_ex.io.ex_sign_divrem
    alu.io.funct3 := id_ex.io.ex_funct3
    alu.io.csr_sen := id_ex.io.ex_csr_sen
    alu.io.csr_rdata := id_ex.io.ex_csr_rdata

    ex_mem.io.ex_alu_result := alu.io.alu_result
    ex_mem.io.ex_reg_wen := id_ex.io.ex_reg_wen
    ex_mem.io.ex_rd := id_ex.io.ex_rd
    ex_mem.io.ex_mem_write_en := id_ex.io.ex_mem_write_en
    ex_mem.io.ex_mem_write_mask := id_ex.io.ex_mem_write_mask 
    ex_mem.io.ex_mem_read_en := id_ex.io.ex_mem_read_en 
    ex_mem.io.ex_mem_read_size := id_ex.io.ex_mem_read_size 
    ex_mem.io.ex_zero_extends := id_ex.io.ex_zero_extends
    ex_mem.io.ex_rs2_rdata := id_ex.io.ex_rs2_rdata
    ex_mem.io.ex_csr_sen := id_ex.io.ex_csr_sen
    ex_mem.io.ex_csr_addr := id_ex.io.ex_csr_addr
    
    mem.io.ACLK := clock
    mem.io.ARESETn := ~(reset.asBool)
    mem.io.mem_addr := ex_mem.io.mem_alu_result
    mem.io.mem_write_data := ex_mem.io.mem_rs2_rdata
    mem.io.mem_write_en := ex_mem.io.mem_mem_write_en
    mem.io.mem_wmask := ex_mem.io.mem_mem_write_mask
    mem.io.mem_read_en := ex_mem.io.mem_mem_read_en
    mem.io.mem_read_size := ex_mem.io.mem_mem_read_size
    mem.io.zero_extends := ex_mem.io.mem_zero_extends
    mem.io.dcache_read_valid := dcache_controller.io.read_cache_fin
    mem.io.dcache_read_data := dcache_controller.io.cache_data
    mem.io.dcache_write_fin := dcache_controller.io.write_cache_fin
    mem.io.direct_read_data := axi_lite_arbiter.io.lsu_direct_read_data
    mem.io.direct_fin := axi_lite_arbiter.io.lsu_direct_fin
    mem.io.crossline_access_stall := dcache_controller.io.crossline_access_stall

    mem_ca.io.mem_alu_result := ex_mem.io.mem_alu_result
    mem_ca.io.mem_reg_wen := ex_mem.io.mem_reg_wen
    mem_ca.io.mem_rd := ex_mem.io.mem_rd
    mem_ca.io.mem_csr_sen := ex_mem.io.mem_csr_sen
    mem_ca.io.mem_csr_addr := ex_mem.io.mem_csr_addr
    mem_ca.io.mem_mem_read_en := ex_mem.io.mem_mem_read_en

    dcache_controller.io.addr := mem.io.dcache_read_addr 
    dcache_controller.io.read_cache_en := mem.io.dcache_read_en 
    dcache_controller.io.write_cache_en := mem.io.dcache_write_en 
    dcache_controller.io.mem_write_fin := axi_lite_arbiter.io.lsu_write_finish 
    dcache_controller.io.mem_read_fin := axi_lite_arbiter.io.lsu_read_valid
    dcache_controller.io.mem_read_data := axi_lite_arbiter.io.lsu_read_data
    dcache_controller.io.write_cache_data := mem.io.dcache_write_data
    dcache_controller.io.write_cache_mask := mem.io.dcache_write_mask
    dcache_controller.io.read_cache_size := mem.io.dcache_read_size 
    io.offset1 := dcache_controller.io.offset1

    stall.io.exit_debugging := id.io.control_signal.exit_debugging
    stall.io.stall_from_inst_if := inst_if.io.stall_from_inst_if
    stall.io.stall_from_mem := mem.io.stall_from_mem
    stall.io.icache_miss := icache_controller.io.cache_miss 
    stall.io.stall_from_alu := alu.io.alu_stall

    axi_lite_arbiter.io.ACLK := clock 
    axi_lite_arbiter.io.ARESETn := ~(reset.asBool)
    axi_lite_arbiter.io.ifu_read_addr := icache_controller.io.mem_addr
    axi_lite_arbiter.io.ifu_read_en := icache_controller.io.mem_read_en
    axi_lite_arbiter.io.lsu_addr := dcache_controller.io.mem_addr
    axi_lite_arbiter.io.lsu_read_en := dcache_controller.io.mem_read_en
    axi_lite_arbiter.io.lsu_write_data := dcache_controller.io.cache_writeback_data
    axi_lite_arbiter.io.lsu_write_en := dcache_controller.io.mem_write_en
    axi_lite_arbiter.io.lsu_direct_read_en := mem.io.direct_read_en 
    axi_lite_arbiter.io.lsu_direct_write_en := mem.io.direct_write_en
    axi_lite_arbiter.io.lsu_direct_write_data := mem.io.direct_write_data 
    axi_lite_arbiter.io.lsu_direct_write_mask := mem.io.dcache_write_mask
    axi_lite_arbiter.io.lsu_direct_addr := mem.io.mem_addr

    ca_wb.io.ca_alu_result := mem_ca.io.ca_alu_result
    ca_wb.io.ca_reg_wen := mem_ca.io.ca_reg_wen
    ca_wb.io.ca_rd := mem_ca.io.ca_rd
    ca_wb.io.ca_csr_sen := mem_ca.io.ca_csr_sen
    ca_wb.io.ca_csr_addr := mem_ca.io.ca_csr_addr
    ca_wb.io.ca_mem_read_data := mem.io.mem_read_data
    ca_wb.io.ca_mem_read_en := mem_ca.io.ca_mem_read_en
}
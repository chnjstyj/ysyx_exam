import chisel3._
import chisel3.util._
import chisel3.experimental._
import java.io.File
import AXI._

class mem_read extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        val ACLK = Input(Clock())
        val ARESETn = Input(Bool())
        val addr = Input(UInt(32.W))
        val en = Input(Bool())
        val valid = Output(Bool())
        val rdata = Output(UInt((AXI.axi_width).W))
        val length = Input(UInt(8.W))

        val axi_master_ar = new axi_master_ar()
        val axi_master_r = new axi_master_r()
    })
    
    //read request channel
    val ARVALID = RegInit(false.B)
    //val ARPROT = RegInit(0.U(3.W))
    val ARADDR = RegInit(0.U(32.W))
    val ARLEN = RegInit(0.U(8.W))  //突发传输长度 LENGTH = ARLENGTH + 1
    val ARSIZE = RegInit(0.U(3.W)) //突发传输宽度 width = 2 ^ ARSIZE
    val ARBURST = RegInit(0.U(2.W)) //突发传输类型 00 FIXED 01 INCR 10 WRAP 11 RESERVED
    val ARID = RegInit(0.U(4.W))

    val ARREADY = Wire(Bool())

    //read data channel
    val RVALID = Wire(Bool())
    val RLAST = Wire(Bool())
    val RDATA = Wire(UInt(AXI.axi_width.W))
    val RRESP = Wire(UInt(2.W))
    val RID = Wire(UInt(4.W))

    val RREADY = RegInit(false.B)

    val arready_recv = RegInit(false.B)
    when (ARREADY){
        arready_recv := true.B
    }.elsewhen (RLAST){
        arready_recv := false.B
    }

    when (io.en) {
        //ARPROT := "b111".U 
        ARID := "b0000".U
        ARADDR := io.addr
        ARLEN := io.length
        ARSIZE := "b101".U
        ARBURST := "b01".U
        when (!arready_recv){
            ARVALID := true.B
        }.otherwise{
            ARVALID := false.B
        }
    }

    when (arready_recv){
        RREADY := true.B
    }.elsewhen (RVALID){
        RREADY := false.B
    }

    when (RVALID){
        io.valid := true.B
        io.rdata := RDATA 
    }.otherwise{
        io.valid := false.B
    }

    io.axi_master_ar.master_arvalid := ARVALID
    io.axi_master_ar.master_araddr := ARADDR
    io.axi_master_ar.master_arlen := ARLEN
    io.axi_master_ar.master_arid := ARID 
    io.axi_master_ar.master_arsize := ARSIZE
    io.axi_master_ar.master_arburst := ARBURST
    ARREADY := io.axi_master_ar.master_arready

    io.axi_master_r.master_rready := RREADY
    RVALID := io.axi_master_r.master_rvalid
    RRESP := io.axi_master_r.master_rresp
    RDATA := io.axi_master_r.master_rdata
    RLAST := io.axi_master_r.master_rlast
    RID := io.axi_master_r.master_rid

    //addPath(new File("playground/src/mem_read.v").getCanonicalPath)
}

class mem_write extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        val ACLK = Input(Clock())
        val ARESETn = Input(Bool())
        val addr = Input(UInt(32.W))
        val en = Input(Bool())
        val wdata = Input(UInt((AXI.axi_width).W))
        val wmask = Input(UInt(4.W))
        val finish = Output(Bool())

        val axi_master_aw = new axi_master_aw()
        val axi_master_w = new axi_master_w()
        val axi_master_b = new axi_master_b()
    })

    

    //addPath(new File("playground/src/mem_write.v").getCanonicalPath)
}
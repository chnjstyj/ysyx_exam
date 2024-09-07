import chisel3._
import chisel3.util._
import chisel3.experimental._
import java.io.File
import AXI._

class mem_read extends Module{
//extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        //val ACLK = Input(Clock())
        //val ARESETn = Input(Bool())
        val addr = Input(UInt(32.W))
        val en = Input(Bool())
        val valid = Output(Bool())
        val rdata = Output(UInt(32.W))
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
    val RDATA = Wire(UInt(32.W))
    val RRESP = Wire(UInt(2.W))
    val RID = Wire(UInt(4.W))

    val RREADY = RegInit(true.B)

    val arready_recv = RegInit(false.B)
    val ARREADY_r = RegNext(ARREADY)
    when (ARREADY_r === false.B && ARREADY === true.B && ARVALID){
        arready_recv := true.B
    }.elsewhen (RLAST){
        arready_recv := false.B
    }

    when (io.en) {
        //ARPROT := "b111".U 
        ARID := "b0000".U
        ARLEN := io.length
        ARSIZE := "b010".U  //4 bytes
        ARBURST := "b01".U
        ARADDR := io.addr
        // when (!arready_recv){
        //     ARVALID := true.B
        // }.otherwise{
        //     ARVALID := false.B
        // }
    }

    val en_r = RegNext(io.en) 
    val en_posedge = WireDefault(io.en && !en_r)

    when (en_posedge){
        ARVALID := true.B
    }.elsewhen (RVALID){
        ARVALID := false.B
    }

    // when (arready_recv){
    //     RREADY := true.B
    // }.elsewhen (RVALID){
    //     RREADY := false.B
    // }

    io.rdata := RDATA 
    io.valid := RVALID
    // when (RVALID){
    //     io.valid := true.B
    // }.otherwise{
    //     io.valid := false.B
    // }

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

class mem_write extends Module{
//extends BlackBox with HasBlackBoxPath{
    val io = IO(new Bundle{
        //val ACLK = Input(Clock())
        //val ARESETn = Input(Bool())
        val addr = Input(UInt(32.W))
        val en = Input(Bool())
        val wdata = Input(UInt(32.W))
        val wmask = Input(UInt(4.W))
        val finish = Output(Bool())
        val length = Input(UInt(8.W))

        val axi_master_aw = new axi_master_aw()
        val axi_master_w = new axi_master_w()
        val axi_master_b = new axi_master_b()
    })

    val AWVALID = RegInit(false.B)
    val AWADDR = RegInit(0.U(32.W))
    val AWID = RegInit(0.U(4.W))
    val AWLEN = RegInit(0.U(8.W))
    val AWSIZE = RegInit(0.U(3.W))
    val AWBURST = RegInit(0.U(2.W))

    val AWREADY = Wire(Bool())

    val WVALID = RegInit(false.B)
    val WDATA = RegInit(0.U(32.W))
    val WSTRB = RegInit(0.U(4.W))
    val WLAST = RegInit(false.B)

    val WREADY = Wire(Bool())

    val BREADY = RegInit(false.B)
    
    val BVALID = Wire(Bool())
    val BRESP = Wire(UInt(2.W))
    val BID = Wire(UInt(4.W))

    io.axi_master_aw.master_awvalid := AWVALID
    io.axi_master_aw.master_awaddr := AWADDR
    io.axi_master_aw.master_awid := AWID
    io.axi_master_aw.master_awlen := AWLEN
    io.axi_master_aw.master_awsize := AWSIZE
    io.axi_master_aw.master_awburst := AWBURST

    io.axi_master_w.master_wvalid := WVALID
    io.axi_master_w.master_wdata := WDATA
    io.axi_master_w.master_wstrb := WSTRB
    io.axi_master_w.master_wlast := WLAST

    io.axi_master_b.master_bready := BREADY

    AWREADY := io.axi_master_aw.master_awready
    WREADY := io.axi_master_w.master_wready
    BVALID := io.axi_master_b.master_bvalid
    BRESP := io.axi_master_b.master_bresp
    BID := io.axi_master_b.master_bid

    val awready_recv = RegInit(false.B)
    when (AWREADY){
        awready_recv := true.B
    }.elsewhen (WLAST){
        awready_recv := false.B
    }

    val AWREADY_r = RegNext(AWREADY)

    val en_r = RegNext(io.en) 
    val en_posedge = WireDefault(io.en && !en_r)

    when (en_posedge){
        AWVALID := true.B
    }.elsewhen (WREADY){
        AWVALID := false.B
    }

    when(io.en) {
        AWADDR := io.addr
        AWID := "b0000".U 
        AWLEN := io.length
        AWSIZE := "b010".U  //4 bytes
        AWBURST := "b01".U
        // when (!awready_recv){
        //     AWVALID := true.B
        // }.otherwise{
        //     AWVALID := false.B
        // }
    }

    val negAWREADY = (!AWREADY).asClock 

    val write_counter = RegInit(0.U(8.W))
    when (AWVALID && AWREADY && !WLAST){
        when (io.length =/= 0.U){
            when (write_counter =/= io.length){
                WVALID := true.B
                WDATA := io.wdata
                WLAST := false.B 
                WSTRB := io.wmask
                write_counter := write_counter + 1.U
            }.elsewhen (write_counter === io.length){
                WVALID := false.B
                WLAST := true.B
                write_counter := 0.U 
            }
        }.otherwise{
            WVALID := true.B
            WDATA := io.wdata
            WLAST := true.B 
            WSTRB := io.wmask
        }
    }.otherwise{
        WVALID := false.B
        WLAST := false.B
    }

    when (AWVALID && AWREADY && !WLAST){
        BREADY := true.B
    }.elsewhen (BVALID){
        BREADY := false.B
    }

    // when (awready_recv && !BVALID){
    //     WREADY := true.B
    // }.elsewhen (BVALID){
    //     WREADY := false.B
    // }

    val finish = WireDefault(false.B) 
    when (BVALID){
        finish := true.B
    }.otherwise{
        finish := false.B
    }
    io.finish := finish

    //addPath(new File("playground/src/mem_write.v").getCanonicalPath)
}
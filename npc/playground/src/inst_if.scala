import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile
import chisel3.util.experimental.loadMemoryFromFileInline
import chisel3.util.HasBlackBoxInline
import java.io.File

class inst_if(image_file:String = "") extends Module{
    val io = IO(new Bundle{
        val ACLK = Input(Clock())
        val ARESETn = Input(Bool())
        val inst_address = Input(UInt(64.W))
        val ce = Input(UInt(1.W))
        val stall_global = Input(UInt(1.W))
        val stall_from_mem_reg = Input(Bool())
        val stall_from_inst_if = Output(UInt(1.W))
        val inst = Output(UInt(32.W))
        //arbiter
        val ifu_read_addr = Output(UInt(32.W))
        val ifu_read_en = Output(Bool())
        val ifu_read_data = Input(UInt(64.W))
        val ifu_read_valid = Input(Bool())

    })

    io.ifu_read_addr := io.inst_address(31,0)
    io.ifu_read_en := io.ce //& !io.stall_from_mem_reg
    val valid = WireDefault(io.ifu_read_valid)

    when (!valid && !io.stall_from_mem_reg){
        io.stall_from_inst_if := 1.U 
    }.otherwise{
        io.stall_from_inst_if := 0.U 
    }

    val inst_before = RegNext(io.inst)

    when (io.stall_from_inst_if.asBool){
        io.inst := 0.U(32.W)
    }.elsewhen (io.stall_from_mem_reg || !valid){
        io.inst := inst_before
    }.otherwise{
        io.inst := io.ifu_read_data(31,0)
    }

    //addPath(new File("/home/tang/ysyx-workbench/npc/playground/src/inst_if.v").getCanonicalPath)
    //addPath(new File("/home/tang/ysyx-workbench/npc/playground/src/mem_rw.v").getCanonicalPath)
    /*
    setInline("inst_if.v",
    """import "DPI-C" function void set_memory_ptr(input logic [31:0] a []);
      |module inst_if(
      |    input clock,
      |    input [63:0] inst_address,
      |    input        ce,
      |    output reg [31:0]  inst
      |);
      |reg [31:0] mem[1023:0];
      |wire [63:0] addr;
      |assign addr = inst_address & 64'h00000000_7fffffff;
      |initial begin
      |    $readmemh("inst.rom",mem);
      |    set_memory_ptr(mem);
      |end
      |always @(*) begin 
      |    if (ce) begin 
      |        inst = mem[addr[11:2]];
      |    end
      |    else begin
      |        inst = 32'h00000000;
      |    end
      |end
      |endmodule
    """.stripMargin)
    */

    /*
    val mem = Mem(1024, UInt(32.W))
    val inst = RegInit(0.U(32.W))

    loadMemoryFromFile(mem,image_file)

    val inst_addr = WireDefault(0.U(64.W))
    inst_addr := (io.inst_address & "h7fff_ffff".U)>>2

    when (io.ce === 1.U){
        io.inst := mem(inst_addr)
    }.otherwise{
        io.inst := "h0000_0000".U
    }
    */
    
}

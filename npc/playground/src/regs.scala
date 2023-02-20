import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile
import chisel3.util.experimental.loadMemoryFromFileInline
import chisel3.util.HasBlackBoxInline

class regs extends BlackBox with HasBlackBoxInline{
    val io = IO(new Bundle{
        val clock = Input(Clock())

        val rs1 = Input(UInt(5.W))
        val rs2 = Input(UInt(5.W))
        val rs1_rdata = Output(UInt(64.W))
        val rs2_rdata = Output(UInt(64.W))

        val rd = Input(UInt(5.W))
        val rd_wdata = Input(UInt(64.W))
        val reg_wen = Input(UInt(1.W))
    })

    setInline("regs.v",
    """import "DPI-C" function void set_gpr_ptr(input logic [63:0] a []);
      |module regs(
      |    input clock,
      |    input [4:0] rs1,
      |    input [4:0] rs2,
      |    output [63:0] rs1_rdata,
      |    output [63:0] rs2_rdata,
      |    input [4:0] rd,
      |    input [63:0] rd_wdata,
      |    input reg_wen
      |);
      |reg [63:0] regs[31:0];
      |integer i;
      |initial begin
      |    for(i = 0; i < 32; i = i + 1) begin
      |      regs[i] = 64'd0;  
      |    end
      |    set_gpr_ptr(regs);
      |end
      |//read
      |assign rs1_rdata = regs[rs1];
      |assign rs2_rdata = regs[rs2];
      |//write
      |always @(posedge clock) begin 
      |    if (reg_wen) begin 
      |        if (rd != 0) begin 
      |            regs[rd] = rd_wdata;
      |        end
      |    end
      |end
      |endmodule
    """.stripMargin)

}
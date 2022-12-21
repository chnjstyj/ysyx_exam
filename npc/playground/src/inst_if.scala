import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile
import chisel3.util.experimental.loadMemoryFromFileInline
import chisel3.util.HasBlackBoxInline

class inst_if(image_file:String = "") extends Module{
    val io = IO(new Bundle{
        val inst_address = Input(UInt(32.W))
        val ce = Input(UInt(1.W))
        val inst = Output(UInt(32.W))
    })
    /*
    setInline("load_image.v",
    """import "DPI-C" function int get_image_name ();
      |module inst_if(
      |    input [31:0] inst_address,
      |    input        ce,
      |    output [31:0] inst
      |);
      |reg [31:0] mem[1023:0];
      |initial begin
      |    $readmemh(get_image_name(),mem);
      |end
      |always @(*) begin 
      |    if (ce) begin 
      |        inst <= mem[inst_address[31:2]];
      |    end
      |    else begin
      |        inst <= 32'h00000000;
      |    end
      |end
      |endmodule
    """.stripMargin)
    */

    val mem = Mem(1024, UInt(32.W))
    val inst = RegInit(0.U(32.W))

    loadMemoryFromFile(mem,image_file)

    val inst_addr = WireDefault(0.U(32.W))
    inst_addr := (io.inst_address & "h7fff_ffff".U)>>2

    when (io.ce === 1.U){
        io.inst := mem(inst_addr)
    }.otherwise{
        io.inst := "h0000_0000".U
    }
    
}

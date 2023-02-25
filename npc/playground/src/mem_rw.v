import "DPI-C" function void pmem_read(
  input longint raddr, output longint rdata);
import "DPI-C" function void pmem_write(
  input longint waddr, input longint wdata, input byte wmask);
wire [63:0] rdata;
/*
always @(*) begin
  pmem_read(raddr, rdata);
  pmem_write(waddr, wdata, wmask);
end
*/

module mem_read(
  input [63:0] addr,
  input en,
  output reg [63:0] rdata
);

always @(*) begin 
  if (en) pmem_read(addr,rdata);
  else rdata = 64'b0;
end

endmodule

module mem_write(
  input clk,
  input [63:0] addr,
  input en,
  input [63:0] wdata,
  input [3:0] wmask
);

always @(posedge clk) begin 
  if (en) begin 
    case (wmask)
      4'b1000:pmem_write(addr,wdata,15);
      4'b0100:pmem_write(addr,wdata,7);
      4'b0010:pmem_write(addr,wdata,3);
      4'b0001:pmem_write(addr,wdata,1);
      default:pmem_write(addr,wdata,15);
    endcase 
  end 
end

endmodule

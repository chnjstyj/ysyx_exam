import "DPI-C" function void pmem_read(
 input bit ARVALID, input int ARADDR, input bit RREADY, output bit ARREADY, output bit RVALID, output bit RLAST, output longint RDATA);
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
  input ACLK,
  input ARESETn,
  input [63:0] addr,
  input en,
  output reg valid,
  output reg [63:0] rdata
  /*
  //read request channel
  output ARVALID,
  input ARREADY,
  output [2:0] ARPROT,
  output [31:0] ARADDR,
  //read data channel
  input  RVALID,
  output  RREADY,
  input RLAST,
  input reg [63:0] RDATA
  */
);

//read request channel
reg ARVALID;
wire ARREADY;
reg [2:0] ARPROT;
reg [31:0] ARADDR;
//read data channel
wire  RVALID;
reg  RREADY;
wire RLAST;
wire [63:0] RDATA;

always @(*) begin 
    pmem_read(ARVALID, ARADDR, RREADY, ARREADY, RVALID, RLAST, RDATA);
end

always @(*) begin 
  if (!ARESETn) begin 
    ARVALID = 1'b0;
    ARPROT = 3'b111;
    ARADDR = 32'b0;
  end 
  else begin 
    ARPROT = 3'b111;
    if (en) begin 
      ARVALID = 1'b1;
      ARADDR = addr[31:0];
    end 
    else begin 
      ARVALID = 1'b0;
      ARADDR = 32'b0;
    end
  end
end

always @(*) begin 
  if (!ARESETn) begin 
    RREADY = 1'b0;
    rdata = 64'b0;
    valid = 1'b0;
  end 
  else begin 
    if (en) begin 
      RREADY = 1'b1;
      valid = 1'b0;
      rdata = 64'b0;
      if (RVALID && RLAST) begin 
        rdata = RDATA;
        valid = 1'b1;
      end 
    end 
    else begin 
      RREADY = 1'b0;
      rdata = 64'b0;
      valid = 1'b1;
    end
  end 
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

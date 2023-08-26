import "DPI-C" function void pmem_read(
 input bit ARVALID, input int ARADDR, input bit RREADY, output bit ARREADY, output bit RVALID, output bit RLAST, output longint RDATA);
import "DPI-C" function void pmem_write(
   input bit AWVALID, input int AWADDR, input bit WVALID, 
   input longint WDATA, input bit WLAST, input logic[3:0] WUSER,
   input bit BREADY,
   output bit AWREADY, output bit WREADY, output bit BVALID);
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
      if (ARREADY && RVALID && RLAST) begin 
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
  input ACLK,
  input ARESETn,
  input [63:0] addr,
  input en,
  input [63:0] wdata,
  input [3:0] wmask,
  output reg finish
);

//write request channel
reg AWVALID;
wire AWREADY;
reg [31:0] AWADDR;
reg [2:0] AWPORT;
//write data channel
reg WVALID;
wire WREADY;
reg [63:0] WDATA;
reg WLAST;
reg [3:0] WUSER;  //equal to wmask
//write response channel
wire BVALID;
reg BREADY;

always @(*) begin 
  pmem_write(AWVALID,AWADDR,WVALID,WDATA,WLAST,WUSER,BREADY,AWREADY,
  WREADY,BVALID);
end

always @(*) begin 
  if (!ARESETn) begin 
    AWVALID = 1'b0;
    AWADDR = 32'b0;
    AWPORT = 3'b111;
  end 
  else begin 
    if (en) begin 
      AWVALID = 1'b1;
      AWADDR = addr[31:0];
      AWPORT = 3'b111;
    end
    else begin 
      AWVALID = 1'b0;
      AWADDR = 32'b0;
      AWPORT = 3'b111;
    end 
  end
end

always @(posedge ACLK or negedge ARESETn) begin 
  if (!ARESETn) begin 
    WVALID = 1'b0;
    WDATA = 64'b0;
    WLAST = 1'b0;
    WUSER = wmask;
  end 
  else begin 
    if (en && AWREADY) begin 
      WVALID = 1'b1;
      WLAST = 1'b1;
      WDATA = wdata;
    end 
    else begin 
      WVALID = 1'b0;
    end 
  end 
end

always @(*) begin 
  if (!ARESETn) begin 
    BREADY = 1'b0;
    finish = 1'b0;
  end 
  else begin
    if (en) begin 
      BREADY = 1'b1;
      if (BVALID)
        finish = 1'b1;
      else 
        finish = 1'b0;
    end 
    else begin 
      BREADY = 1'b0;
      finish = 1'b0;
    end
  end
end

/*
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
*/

endmodule

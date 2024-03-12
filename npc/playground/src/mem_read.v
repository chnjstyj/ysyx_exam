import "DPI-C" function void pmem_read(
 input bit ARVALID, input int ARADDR, input bit RREADY, 
 output bit ARREADY, output bit RVALID, output bit RLAST, output logic[255:0] RDATA, 
 output logic[1:0] RRESP, output byte ARLEN, output logic[2:0] ARSIZE, output logic[1:0] ARBURST);
module mem_read(
  input ACLK,
  input ARESETn,
  input [31:0] addr,
  input en,
  output wire valid,
  output wire [255:0] rdata
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
reg [7:0] ARLEN; //突发传输长度 
reg [2:0] ARSIZE; //突发传输宽度 width = 2 ^ ARSIZE
reg [1:0] ARBURST; //突发传输类型  00 FIXED 01 INCR 10 WRAP 11 RESERVED
//read data channel
wire  RVALID;
reg  RREADY;
wire RLAST;
wire [255:0] RDATA;
wire [1:0] RRESP;

always @(posedge ACLK) begin 
    pmem_read(ARVALID, ARADDR, RREADY, ARREADY, RVALID, RLAST, RDATA, RRESP, ARLEN, ARSIZE, ARBURST);
end

always @(*) begin 
  if (!ARESETn) begin 
    ARVALID = 1'b0;
    ARPROT = 3'b111;
    ARADDR = 32'b0;
    ARLEN = 8'b0;
    ARSIZE = 3'b101; //256 bits
    ARBURST = 2'b01;
  end 
  else begin 
    ARPROT = 3'b111;
    ARLEN = 8'b0;
    ARSIZE = 3'b101; //256 bits
    ARBURST = 2'b01;
    if (en) begin 
      ARVALID = 1'b1;
      ARADDR = addr;
    end 
    else begin 
      ARVALID = 1'b0;
      ARADDR = addr;
    end
  end
end

always @(*) begin 
  if (!ARESETn) begin 
    RREADY = 1'b0;
    //valid = 1'b0;
  end 
  else begin 
    if (en) begin 
      RREADY = 1'b1;
      /*
      if (ARREADY && RVALID && RLAST) begin 
        valid = 1'b1;
      end 
      else begin 
        valid = 1'b0;
      end*/
    end 
    else begin 
      RREADY = 1'b0;
      //valid = 1'b0;
    end
  end 
end

assign valid = (en && ARREADY && RVALID && RLAST) ? 1'b1 : 1'b0;

assign rdata = RDATA;

endmodule
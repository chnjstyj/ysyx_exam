import "DPI-C" function void pmem_write(
   input bit AWVALID, input int AWADDR, input bit WVALID, 
   input longint WDATA, input bit WLAST, input logic[7:0] WSTRB,
   input bit BREADY,
   output bit AWREADY, output bit WREADY, output bit BVALID, output logic[1:0] BRESP);
module mem_write(
  input ACLK,
  input ARESETn,
  input [31:0] addr,
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
reg [7:0] WSTRB;  //equal to wmask
//write response channel
wire BVALID;
wire [1:0] BRESP;
reg BREADY;

always @(posedge ACLK) begin 
  pmem_write(AWVALID,AWADDR,WVALID,WDATA,WLAST,WSTRB,BREADY,AWREADY,
  WREADY,BVALID,BRESP);
end

always @(*) begin 
  if (!ARESETn) begin 
    AWVALID = 1'b0;
    AWADDR = 32'b0;
    AWPORT = 3'b111;
  end 
  else begin 
    if (en) begin 
      AWADDR = addr;
      AWVALID = 1'b1;
      AWPORT = 3'b111;
    end
    else begin 
      AWADDR = 32'b0;
      AWVALID = 1'b0;
      AWPORT = 3'b111;
    end 
  end
end

always @(*) begin 
  if (!ARESETn) begin 
    WVALID = 1'b0;
    WDATA = 64'b0;
    WLAST = 1'b0;
    //WUSER = 8'hff;
  end 
  else begin 
    if (en) begin 
      WVALID = 1'b1;
      WLAST = 1'b1;
      WDATA = wdata;
      //WUSER = wmask;
    end 
    else begin 
      WVALID = 1'b0;
      WLAST = 1'b0;
      WDATA = 64'b0;
      //WUSER = wmask;
    end 
  end 
end

always @(*) begin 
  case (wmask) 
    4'b1000:WSTRB = 8'b11111111; 
    4'b0100:WSTRB = 8'b00001111;
    4'b0010:WSTRB = 8'b00000011;
    4'b0001:WSTRB = 8'b00000001;
  endcase 
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
always @(negedge ACLK) begin 
  if (!ARESETn) begin 
    finish <= 1'b0;
  end 
  else begin
    if (BVALID)
      finish <= 1'b1;
    else 
      finish <= 1'b0;
  end
end
*/

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

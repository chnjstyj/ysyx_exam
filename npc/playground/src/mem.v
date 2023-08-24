module mem(
    input ACLK,
    input ARESETn,
    input mem_write_en,
    input [63:0] mem_write_data,
    input [63:0] mem_addr,
    input [3:0] mem_wmask,
    input mem_read_en,
    input [3:0] mem_read_size,
    input zero_extends,
    output stall_from_mem,
    output reg [63:0] mem_read_data
);

wire [63:0] rdata;
wire sign;
wire valid;

assign stall_from_mem = valid;

always @(*) begin 
    case (mem_read_size) 
        4'b1000:mem_read_data = rdata;
        4'b0100:begin 
            mem_read_data = zero_extends?{{32{1'd0}},rdata[31:0]}:{{32{rdata[31]}},rdata[31:0]};
        end
        4'b0010:begin 
            mem_read_data = zero_extends?{{48{1'd0}},rdata[15:0]}:{{48{rdata[15]}},rdata[15:0]};
        end
        4'b0001:begin 
            mem_read_data = zero_extends?{{56{1'd0}},rdata[7:0]}:{{56{rdata[7]}},rdata[7:0]};
        end
        default:mem_read_data = rdata;
    endcase 
end

mem_write u_mem_write(
    .clk(ACLK),
    .addr(mem_addr),
    .en(mem_write_en),
    .wdata(mem_write_data),
    .wmask(mem_wmask)
);

mem_read u_mem_read(
    .ACLK(ACLK),
    .ARESETn(ARESETn),
    .addr(mem_addr),
    .en(mem_read_en),
    .valid(valid),
    .rdata(rdata)
);

endmodule
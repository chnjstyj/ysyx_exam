module mem(
    input clock,
    input mem_write_en,
    input [63:0] mem_write_data,
    input [63:0] mem_addr,
    input [3:0] mem_wmask,
    input mem_read_en,
    input [3:0] mem_read_size,
    output reg [63:0] mem_read_data
);

wire [63:0] rdata;

always @(*) begin 
    case (mem_read_size) 
        4'b1000:mem_read_data = rdata;
        4'b0100:mem_read_data = {{32{rdata[31]}},rdata[31:0]};
        4'b0010:mem_read_data = {{48{rdata[15]}},rdata[15:0]};
        4'b0001:mem_read_data = {{56{rdata[7]}},rdata[7:0]};
        default:mem_read_data = rdata;
    endcase 
end

mem_write u_mem_write(
    .clk(clock),
    .addr(mem_addr),
    .en(mem_write_en),
    .wdata(mem_write_data),
    .wmask(mem_wmask)
);

mem_read u_mem_read(
    .addr(mem_addr),
    .en(mem_read_en),
    .rdata(rdata)
);

endmodule
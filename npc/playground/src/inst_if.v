import "DPI-C" function void set_memory_ptr(input logic [31:0] a []);
module inst_if(
input clock,
input [63:0] inst_address,
input        ce,
output reg [31:0]  inst
);
reg [31:0] mem[1023:0];
wire [63:0] addr;
assign addr = inst_address & 64'h00000000_7fffffff;
initial begin
    $readmemh("inst.rom",mem);
    set_memory_ptr(mem);
end
always @(*) begin 
    if (ce) begin 
        inst = mem[addr[11:2]];
    end
    else begin
        inst = 32'h00000000;
    end
end
endmodule
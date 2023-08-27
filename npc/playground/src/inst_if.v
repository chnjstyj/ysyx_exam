import "DPI-C" function void set_memory_ptr(input logic [31:0] a []);
module inst_if(
input ACLK,
input ARESETn,
input [63:0] inst_address,
input        ce,
input stall_global,
output reg stall_from_inst_if,
output reg [31:0]  inst
);
reg [31:0] mem[1023:0];
wire [63:0] addr;
wire [63:0] rdata;
wire valid;
//assign addr = inst_address & 64'h00000000_7fffffff;

//assign stall_from_inst_if = !valid && ce;
always @(*) begin 
    if (!valid && ce) stall_from_inst_if = 1'b1;
    else stall_from_inst_if = 1'b0;
end

reg [31:0] inst_before;

always @(posedge ACLK) begin 
    if (valid)
        inst_before <= rdata[31:0];
end

mem_read if_mem_read(
    .ACLK(ACLK),
    .ARESETn(ARESETn),
    .addr(inst_address),
    .en(ce),
    .valid(valid),
    .rdata(rdata)
);

always @(*) begin 
    if (!stall_global)
        inst = rdata[31:0];
    else 
        inst = inst_before;
end



/*
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
end*/

endmodule
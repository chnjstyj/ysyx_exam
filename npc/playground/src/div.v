module div(
    input [63:0] data_a,
    input [63:0] data_b,
    input alu_result_size,
    output signed [63:0] result
);

wire [63:0] r;

assign r = $signed(data_a) / $signed(data_b);

wire [31:0] faw;
wire [31:0] fbw;
wire [31:0] rw;

assign faw = data_a[31:0];
assign fbw = data_b[31:0];

assign rw = $signed(faw) / $signed(fbw);

assign result = alu_result_size == 1'b1 ? {{32{1'b0}},rw} : r;

endmodule
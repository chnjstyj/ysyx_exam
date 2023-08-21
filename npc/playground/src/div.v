module div(
    input [63:0] data_a,
    input [63:0] data_b,
    output signed [63:0] result
);

wire [63:0] fa;
wire [63:0] fb;
wire [63:0] r;
wire [63:0] sr;

assign fa = data_a[63] ? -data_a : data_a;
assign fb = data_b[63] ? -data_b : data_b;
assign r = fa / fb;
assign sr = (data_a[63] ^ data_b[63]) ? -r : r;

assign result = sr;

endmodule
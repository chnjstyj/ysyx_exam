module mem(
    input clock,
    input mem_write_en,
    input [63:0] mem_write_data,
    input [63:0] mem_write_addr,
    input [3:0] mem_wmask

);

/*
mem_read u_mem_read(

);*/

mem_write u_mem_write(
    .clk(clock),
    .addr(mem_write_addr),
    .en(mem_write_en),
    .wdata(mem_write_data),
    .wmask(mem_wmask)
);

endmodule
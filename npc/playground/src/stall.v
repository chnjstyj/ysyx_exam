module stall_v(
    input clock,
    input stall_from_inst_if,
    input stall_from_mem,
    output wire stall_global
);

reg stall_inst_if_reg;
reg stall_mem_reg;

always @(negedge clock) begin 
    stall_inst_if_reg <= stall_from_inst_if;
    stall_mem_reg <= stall_from_mem;
end 

assign stall_global = stall_inst_if_reg | stall_mem_reg;

endmodule
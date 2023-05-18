import "DPI-C" function void set_gpr_ptr(input logic [63:0] a []);
module regs(
input clock,
input [4:0] rs1,
input [4:0] rs2,
input [11:0] csr_addr,
output [63:0] rs1_rdata,
output [63:0] rs2_rdata,
output reg [63:0] csr_rdata,
input [4:0] rd,
input [63:0] rd_wdata,
input reg_wen,
input csr_wen,
input csr_sen
);
reg [63:0] regs[31:0];
wire [63:0] csr_wdata = csr_wen ? rs1_rdata : 
csr_sen ? rd_wdata : 64'h0;
//immI[7:0]
//[11:10] read/write
//[9:8] lowest privilege level
//mepc
reg [63:0] mepc;
reg [63:0] mstatus;
reg [63:0] mcause;


//csr read
//assign csr_rdata = csrs[csr_addr[7:0]];
always @(*) begin 
    case (csr_addr[11:0])
    12'h341:csr_rdata = mepc;
    12'h300:csr_rdata = mstatus;
    12'h342:csr_rdata = mcause;
    default:csr_rdata = 64'h0;
    endcase
end
//csr write
always @(posedge clock) begin 
    if (csr_wen || csr_sen) begin 
        case (csr_addr[11:0])
        12'h341:mepc <= csr_wdata;
        12'h300:mstatus <= csr_wdata;
        12'h342:mcause <= csr_wdata;
        default:;
        endcase
    end
end

integer i;
initial begin
    for(i = 0; i < 32; i = i + 1) begin
      regs[i] = 64'd0;  
    end
    set_gpr_ptr(regs);
end
//read
assign rs1_rdata = regs[rs1];
assign rs2_rdata = regs[rs2];
//write
always @(posedge clock) begin 
    if (reg_wen) begin 
        if (rd != 0) begin 
            regs[rd] = rd_wdata;
        end
    end
end

endmodule
module csrs(
input clock,
input [11:0] csr_addr,
input [63:0] rs1_rdata,
output reg [63:0] csr_rdata,
input [63:0] rd_wdata,
input csr_wen,
input csr_sen,
input ecall,
input [63:0] ecall_idx,
input [63:0] pc,
output wire [63:0] mret_addr
);

wire [63:0] csr_wdata = csr_wen ? rs1_rdata : 
csr_sen ? rd_wdata : 64'h0;
//immI[7:0]
//[11:10] read/write
//[9:8] lowest privilege level
//mepc
reg [63:0] mepc;
reg [63:0] mstatus;
reg [63:0] mcause;
reg [63:0] mtvec;

assign mret_addr = mepc;

//csr read
//assign csr_rdata = csrs[csr_addr[7:0]];
always @(*) begin 
    if (ecall == 1'b1) begin 
        csr_rdata = mtvec;
    end 
    else begin
        case (csr_addr[11:0])
        12'h341:csr_rdata = mepc;
        12'h300:csr_rdata = mstatus;
        12'h342:csr_rdata = mcause;
        12'h305:csr_rdata = mtvec;
        default:csr_rdata = 64'h0;
        endcase
    end                     
end
//csr write
always @(posedge clock) begin 
    if (csr_wen || csr_sen) begin 
        case (csr_addr[11:0])
        12'h341:mepc <= csr_wdata;
        12'h300:mstatus <= csr_wdata;
        12'h342:mcause <= csr_wdata;
        12'h305:mtvec <= csr_wdata;
        default:;
        endcase
    end
    else if (ecall) begin 
        mepc <= pc;
        mcause <= ecall_idx;
    end
end

endmodule
module csrs(
input clock,
input [11:0] csr_addr,
input [63:0] rs1_rdata,
output reg [63:0] csr_rdata,
input [63:0] rd_wdata,
input csr_wen,
input csr_sen
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


//csr read
//assign csr_rdata = csrs[csr_addr[7:0]];
always @(*) begin 
    case (csr_addr[11:0])
    12'h341:csr_rdata = mepc;
    12'h300:csr_rdata = mstatus;
    12'h342:csr_rdata = mcause;
    12'h305:csr_rdata = mtvec;
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
        12'h305:mtvec <= csr_wdata;
        default:;
        endcase
    end
end

endmodule
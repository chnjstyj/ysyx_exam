#ifndef TB_TOP
#define TB_TOP

#include <stdint.h>
#include "ftrace.h"
#include "svdpi.h"

#define PMEM_SIZE 0x8000000

uint64_t isa_reg_str2val(const char *s, bool *success);

void cpu_exec(int steps);
void exit_npc();

extern uint64_t* pc;
extern uint32_t* memory;
extern uint64_t* gpr;

extern "C" void pmem_read(
  svBit ARVALID, int ARADDR, svBit RREADY, svBit* ARREADY, svBit* RVALID, svBit* RLAST, svLogicVecVal* RDATA, svLogicVecVal* RRESP, char* ARLEN, svLogicVecVal* ARSIZE, svLogicVecVal* ARBURST);

extern int ftrace_func_nums;
extern ftrace_info* ftrace_infos;
#endif

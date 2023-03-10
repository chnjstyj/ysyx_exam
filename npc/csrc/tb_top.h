#ifndef TB_TOP
#define TB_TOP

#include <stdint.h>
#include "ftrace.h"

#define PMEM_SIZE 0x8000000

uint64_t isa_reg_str2val(const char *s, bool *success);

void cpu_exec(int steps);
void exit_npc();

extern uint64_t* pc;
extern uint32_t* memory;
extern uint64_t* gpr;

extern "C" void pmem_read(long long raddr, long long *rdata);

extern int ftrace_func_nums;
extern ftrace_info* ftrace_infos;
#endif

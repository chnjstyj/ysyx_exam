#ifndef TB_TOP
#define TB_TOP

#include <stdint.h>
#include "ftrace.h"

uint64_t isa_reg_str2val(const char *s, bool *success);

void cpu_exec(int steps);
void exit_npc();

extern uint64_t* pc;
extern uint32_t* memory;
extern uint64_t* gpr;
extern char pmem[];

extern int ftrace_func_nums;
extern ftrace_info* ftrace_infos;
#endif

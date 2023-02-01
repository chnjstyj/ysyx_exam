#ifndef TB_TOP
#define TB_TOP

#include <stdint.h>

extern char* regs[];

void cpu_exec(int steps);
void exit_npc();

extern uint64_t pc;
extern uint32_t* memory;
extern uint64_t* gpr;

#endif

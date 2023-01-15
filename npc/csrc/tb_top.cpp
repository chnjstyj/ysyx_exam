#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <verilated.h>
#include <verilated_vcd_c.h>
//#include <nvboard.h>
#include <iostream>
#include <unistd.h>

#include "Vtop.h"
#include "sdb.h"

#include "svdpi.h"
#include "Vtop__Dpi.h"
#include "verilated_dpi.h"

#include <readline/readline.h>
#include <readline/history.h>

const char *regs[] = {
  "$0", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
  "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
  "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
  "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
};

static VerilatedVcdC* m_trace = new VerilatedVcdC;
static Vtop* top = new Vtop;

static char* image_name = NULL;

#define MAX_SIM_TIME 100
vluint64_t sim_time = 0;

//void nvboard_bind_all_pins(Vtop *top);

uint32_t* memory = NULL;
uint64_t* gpr = NULL;
uint64_t pc = top->io_inst_address;

void exit_()
{
  m_trace->close();
  top->final();
  delete top;
  //nvboard_quit();
  printf("ebreak\nHIT GOOD TRAP!\n");
  printf("%x\n%x\n",*memory,*(memory+1));

  exit(0);
}

extern "C" void set_memory_ptr(const svOpenArrayHandle r)
{
  memory = (uint32_t *)(((VerilatedDpiOpenVar*)r)->datap());
}

extern "C" void set_gpr_ptr(const svOpenArrayHandle r)
{
  gpr = (uint64_t *)(((VerilatedDpiOpenVar*)r)->datap());
}

static void single_cycle(Vtop* top)
{
  top->clock = 1;top->eval();
  sim_time++;
  m_trace->dump(sim_time);
  top->clock = 0;top->eval();
  sim_time++;
  m_trace->dump(sim_time);
}

void cpu_exec(int steps)
{
  if (steps == -1)
  {
    while (1)
    {
      single_cycle(top);
    }
  }
  int i = steps;
  for (;i > 0; i --)
  {
    single_cycle(top);
  }
}

static void reset(int n,Vtop* top) {
  top->reset = 1;
  while (n -- > 0) single_cycle(top);
  top->reset = 0;
}

int main(int argc,char *argv[])
{
  //Vtop *top = new Vtop;
  Verilated::traceEverOn(true);
  //VerilatedVcdC *m_trace = new VerilatedVcdC;
  top->trace(m_trace, 10);
  m_trace->open("waveform.vcd");
  //nvboard_bind_all_pins(&top);
  //nvboard_init();
  reset(2,top);
  while (
    //sim_time <= 200
    1
    )
  {
    //single_cycle(top);
    //nvboard_update();
    sdb_mainloop();
  }

  m_trace->close();
  top->final();
  delete top;
  //nvboard_quit();
  printf("HIT BAD TRAP\n");
  return 1;
}

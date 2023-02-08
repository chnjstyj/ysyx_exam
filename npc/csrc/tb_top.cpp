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
#include "disasm.h"
#include "ftrace.h"
#include "expr.h"

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

//itrace
static int iringbuf_head;
static char iringbuf[16][30];

//ftrace
int ftrace_func_nums = 0;
ftrace_info* ftrace_infos = NULL;
bool ftrace_enable = false;

void print_itrace_buf()
{
  int end_point = iringbuf_head;
  do 
  {
    if (iringbuf_head - 1 < 0)
    {
      iringbuf_head = 15;
    }
    else 
    {
      iringbuf_head--;
    }
    printf("%s\n",iringbuf[iringbuf_head]);
  }
  while (iringbuf_head != end_point);
}

void call_ftrace_handle()
{
  printf("ftrace\n");
  if (ftrace_enable == true) 
    printf("ftrace with elf\n");
}

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

void exit_npc()
{
  m_trace->close();
  top->final();
  delete top;
  //nvboard_quit();
  printf("exit\nHIT BAD TRAP!\n");
  print_itrace_buf();
  exit(1);
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
  int i = steps;
  int j = 0;
  char str[30] = "";
  if (steps == -1)
  {
    while (j < 10000)
    {
      j++;
      single_cycle(top);
      disassemble(str,96,top->io_inst_address,(uint8_t*)&(top->io_inst),4);
      strcpy(iringbuf[iringbuf_head],str);
      if (iringbuf_head == 15)
      {
        iringbuf_head = 0;
      }
      else 
      {
        iringbuf_head++;
      }
    }
  }
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
  init_disasm("riscv64-pc-linux-gnu");
  init_regex();
  if (argc >= 2 && strcmp("elf",argv[1]) == 0)
  {
    ftrace_enable = true;
    ftrace_infos = init_ftrace("inst_rom.elf",&ftrace_func_nums);
  }
  //nvboard_bind_all_pins(&top);
  //nvboard_init();
  reset(2,top);
  sdb_mainloop();
  /*
  while (
    //sim_time <= 200
    1
    )
  {
    //single_cycle(top);
    //nvboard_update();
  }*/

  m_trace->close();
  top->final();
  delete top;
  //nvboard_quit();
  printf("HIT BAD TRAP\n");
  return 1;
}

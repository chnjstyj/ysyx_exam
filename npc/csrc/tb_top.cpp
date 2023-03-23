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
#include "common.h"
#include "tb_top.h"
#include "dl.h"
#include "mtrace.h"

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

uint64_t isa_reg_str2val(const char *s, bool *success) 
{
  int i;
  for (i = 0; i < 32; i++)
  {
    if (!strcmp(*(regs+i),s))
    {
      *success = true;
      return gpr[i];
    }
  }
  *success = false;
  return 0;
}

static VerilatedVcdC* m_trace = new VerilatedVcdC;
static Vtop* top = new Vtop;

static char* image_name = NULL;

#define MAX_SIM_TIME 100
vluint64_t sim_time = 0;

//void nvboard_bind_all_pins(Vtop *top);

uint32_t* memory = NULL;
uint64_t* gpr = NULL;
uint64_t* pc = &(top->io_inst_address);

//memory
static uint8_t pmem[PMEM_SIZE] = {0};

//itrace
static int iringbuf_head;
static char iringbuf[16][30];

//ftrace
int ftrace_func_nums = 0;
ftrace_info* ftrace_infos = NULL;
bool ftrace_enable = false;
#define INST top->io_inst
#define INST_ADDR top->io_inst_address
#define NEXT_INST_ADDR top->io_next_inst_address

//diff
bool diff_enable = false;
static int total_steps;

void init_pmem(const char* file_name)
{
  FILE* fp = fopen(file_name,"r");
  if (!fp)
  {
    printf("Error file\n");
    assert(0);
  }
  fread(pmem,1,PMEM_SIZE,fp);
  fclose(fp);
}

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
    if (iringbuf[iringbuf_head][0] != 0)
      printf("%s\n",iringbuf[iringbuf_head]);
  }
  while (iringbuf_head != end_point);
}

void call_ftrace_handle()
{
  if (BITS(INST,6,0) == 0b1101111) //jal 
  {
    update_ftrace(INST_ADDR,NEXT_INST_ADDR);
  }
  else if (BITS(INST,14,12) == 0 && BITS(INST,6,0) == 0b1100111) //jalr
  {
    BITS(INST, 19, 15) == 1 ? ret(INST_ADDR) : 
    update_ftrace(INST_ADDR,NEXT_INST_ADDR);
  }
}


//dpi-c

extern "C" void pmem_read(long long raddr, long long *rdata) {
  // 总是读取地址为`raddr & ~0x7ull`的8字节返回给`rdata`
  int i;
  long long addr = raddr & 0x7fffffff;
  *rdata = 0;
  unsigned long long temp;
  update_mtrace("read",raddr);
  for (i = 0; i < 8; i++)
  {
    temp = (pmem[addr + i] & 0xff);
    temp = temp << (8 * i);
    *rdata |= temp;
  }
}

extern "C" void pmem_write(long long waddr, long long wdata, char wmask) {
  // 总是往地址为`waddr & ~0x7ull`的8字节按写掩码`wmask`写入`wdata`
  // `wmask`中每比特表示`wdata`中1个字节的掩码,
  // 如`wmask = 0x3`代表只写入最低2个字节, 内存中的其它字节保持不变
  long long addr = waddr & 0x7fffffff;
  char size;
  int i;
  size = (wmask + 1)/2;
  update_mtrace("write",waddr);
  for (i = 0; i < size; i ++)
  {
    pmem[addr + i] = (uint8_t)(wdata >> 8 * i);
  }
}

void exit_ebreak()
{
  m_trace->close();
  top->final();
  delete top;
  //nvboard_quit();
  printf("ebreak\nHIT GOOD TRAP!\n");
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
  printf("total steps:%d\n",total_steps);
  exit(1);
}
/*
extern "C" void set_memory_ptr(const svOpenArrayHandle r)
{
  memory = (uint32_t *)(((VerilatedDpiOpenVar*)r)->datap());
}
*/

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
  char str[50] = {0};
  if (steps == -1)
  {
    while (j < 10000)
    {
      j++;
      total_steps++;
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
      if (diff_enable == true)
        difftest_step();
    }
  }
  for (;i > 0; i --)
  {
    total_steps++;
    single_cycle(top);
    disassemble(str,96,INST_ADDR,(uint8_t*)&(INST),4);
    printf("%lx %s\n",top->io_inst_address,str);
    if (diff_enable == true)
      difftest_step();
  }
}

static void reset(int n,Vtop* top) {
  top->reset = 1;
  while (n -- > 0) single_cycle(top);
  top->reset = 0;
}

int main(int argc,char *argv[])
{
  int i;
  Verilated::traceEverOn(true);
  //VerilatedVcdC *m_trace = new VerilatedVcdC;
  top->trace(m_trace, 10);
  m_trace->open("waveform.vcd");
  reset(2,top);
  
  //initial steps
  init_pmem("inst.bin");
  init_disasm("riscv64-pc-linux-gnu");
  init_regex();
  for (i = 0; i < argc; i++)
  {
    if (strcmp("elf",argv[i]) == 0)
    {
      ftrace_enable = true;
      ftrace_infos = init_ftrace("inst_rom.elf",&ftrace_func_nums);
    }
    if (strcmp("diff",argv[i]) == 0)
    {
      diff_enable = true;
      init_difftest("../nemu/build/riscv64-nemu-interpreter-so");
    }
  }

  //nvboard_bind_all_pins(&top);
  //nvboard_init();
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
  void print_mtrace_message();
  printf("HIT BAD TRAP\n");
  return 1;
}

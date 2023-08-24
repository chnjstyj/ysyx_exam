#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <verilated.h>
#include <verilated_vcd_c.h>
//#include <nvboard.h>
#include <iostream>
#include <unistd.h>
#include <sys/time.h>

#include "Vtop.h"
#include "sdb.h"
#include "disasm.h"
#include "ftrace.h"
#include "expr.h"
#include "common.h"
#include "tb_top.h"
#include "dl.h"
#include "mtrace.h"
#include "gpu.h"

#include "svdpi.h"
#include "Vtop__Dpi.h"
#include "verilated_dpi.h"

#include <readline/readline.h>
#include <readline/history.h>

//#define waveform 1
//#define mtrace_ 1
//#define itrace_ 1

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
//static uint8_t pmem[PMEM_SIZE] = {0};
static uint8_t* pmem = NULL;

//itrace
static int iringbuf_head;
static char iringbuf[16][30];
FILE* itrace;

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
  pmem = (uint8_t*)malloc(PMEM_SIZE);
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
  if (ftrace_enable)
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
}

static uint64_t boot_time = 0; 

//dpi-c
extern "C" void pmem_read(long long raddr, long long *rdata) {
  // 总是读取地址为`raddr & ~0x7ull`的8字节返回给`rdata`
  int i;
  *rdata = 0;
  unsigned long long temp;
  if (raddr == RTC_ADDR)
  {
    struct timeval now;
    gettimeofday(&now, NULL);
    uint64_t us = now.tv_sec * 1000000 + now.tv_usec;
    if (boot_time == 0) boot_time = us;
    //printf("%lld\n",us - boot_time);
    *rdata = (long long)(us - boot_time);
  }
  else if (raddr == VGACTL_ADDR)
  {
    *rdata = SCREEN_W << 16 | SCREEN_H;
  }
  else if (raddr >= 0x80000000 && raddr < 0x80000000 + PMEM_SIZE)
  {
    long long addr = raddr & 0x7fffffff;
    #ifdef mtrace_
    update_mtrace("read",raddr);
    #endif
    for (i = 0; i < 8; i++)
    {
      temp = (pmem[addr + i] & 0xff);
      temp = temp << (8 * i);
      *rdata |= temp;
    }
  }
  else 
  {
    printf("invalid read address %llx\n",raddr);
    printf("total steps:%d\n",total_steps);
    fclose(itrace);
    assert(0);
  }
}

extern "C" void pmem_write(long long waddr, long long wdata, char wmask) {
  // 总是往地址为`waddr & ~0x7ull`的8字节按写掩码`wmask`写入`wdata`
  // `wmask`中每比特表示`wdata`中1个字节的掩码,
  // 如`wmask = 0x3`代表只写入最低2个字节, 内存中的其它字节保持不变
  char size;
  int i;
  size = (wmask + 1)/2;
  #ifdef mtrace_ 
  update_mtrace("write",waddr);
  #endif
  if (waddr == SERIAL_PORT)
  {
    putchar((uint8_t)wdata);
  }
  else if (waddr == SYNC_ADDR)
  {
    vgasync = (uint8_t)wdata;
  }
  else if (waddr >= FB_ADDR && waddr < FB_ADDR + SCREEN_W * SCREEN_H * sizeof(uint32_t))
  {
    uint64_t addr = (uint64_t)waddr - FB_ADDR;
    for (i = 0; i < size; i ++)
    {
      *((uint8_t*)vmem + addr + i) = (uint8_t)(wdata >> 8 * i);
    }
  }
  else if (waddr >= 0x80000000 && waddr < 0x80000000 + PMEM_SIZE)
  {
    uint64_t addr = (uint64_t)waddr & (uint64_t)0x7fffffff;
    for (i = 0; i < size; i ++)
    {
      pmem[addr + i] = (uint8_t)(wdata >> 8 * i);
    }
  }
  else 
  {
    printf("invalid write address %llx\n",waddr);
    printf("total steps:%d\n",total_steps);
    fclose(itrace);
    assert(0);
  }
}

void exit_ebreak()
{
  #ifdef waveform
  m_trace->close();
  #endif
  top->final();
  delete top;
  delete pmem;
  delete ftrace_infos;
  fclose(flog_file);
  //nvboard_quit();
  printf("ebreak\nHIT GOOD TRAP!\n");
  printf("total steps:%d\n",total_steps);
  exit(0);
}

void exit_npc()
{
  #ifdef waveform
  m_trace->close();
  #endif
  top->final();
  delete top;
  delete pmem;
  delete ftrace_infos;
  fclose(flog_file);
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

void single_cycle(Vtop* top)
{
  top->clock = 1;top->eval();
  sim_time++;
  #ifdef waveform
  m_trace->dump(sim_time);
  #endif
  top->clock = 0;top->eval();
  sim_time++;
  #ifdef waveform
  m_trace->dump(sim_time);
  #endif
}

void inline update_device()
{
  vga_update_screen();
}

void cpu_exec(int steps)
{
  int i = steps;
  int j = 0;
  char str[50] = {0};
  if (steps == -1)
  {
    while (1)
    {
      total_steps++;
      single_cycle(top);
      #ifdef itrace_
      disassemble(str,96,top->io_inst_address,(uint8_t*)&(top->io_inst),4);
      strcpy(iringbuf[iringbuf_head],str);
      fprintf(itrace,str);
      if (iringbuf_head == 15)
      {
        iringbuf_head = 0;
      }
      else 
      {
        iringbuf_head++;
      }
      #endif
      j++;
      if (j == 25600)
      {
        j = 0;
        update_device();
      }
      if (diff_enable == true)
      {
        uint32_t inst = top->io_inst;
        uint32_t inst_6_0 = inst & 0x7f;
        uint32_t inst_31_20 = (inst & 0xfff00000) >> 20;
        uint32_t inst_31_25 = (inst & 0xfe000000) >> 20;
        uint32_t inst_11_7  = (inst & 0xf80     ) >> 7 ;
        uint32_t inst_19_15 = (inst & 0xf8000   ) >> 15;
        uint32_t offset = inst_6_0 == 3 ? inst_31_20: inst_31_25 | inst_11_7;
        uint32_t address = offset + gpr[inst_19_15];
        if ((inst_6_0 == 3 || inst_6_0 == 35) && address > 0x90000000)
        {
          difftest_skip();
        }
        else
         difftest_step();
      }
    }
  }
  for (;i > 0; i --)
  {
    total_steps++;
    single_cycle(top);
    disassemble(str,96,INST_ADDR,(uint8_t*)&(INST),4);
    printf("%lx %s\n",top->io_inst_address,str);
    if (diff_enable == true)
    {
      uint32_t inst = top->io_inst;
      uint32_t inst_6_0 = inst & 0x7f;
      uint32_t inst_31_20 = (inst & 0xfff00000) >> 20;
      uint32_t inst_31_25 = (inst & 0xfe000000) >> 20;
      uint32_t inst_11_7  = (inst & 0xf80     ) >> 7 ;
      uint32_t inst_19_15 = (inst & 0xf8000   ) >> 15;
      uint32_t offset = inst_6_0 == 3 ? inst_31_20: inst_31_25 | inst_11_7;
      uint32_t address = offset + gpr[inst_19_15];
      if ((inst_6_0 == 3 || inst_6_0 == 35) && address > 0x90000000)
      {
        printf("skip\n");
        difftest_skip();
      }
      else
        difftest_step();
    }
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
  #ifdef waveform
  Verilated::traceEverOn(true);
  top->trace(m_trace, 10);
  m_trace->open("./waveform.vcd");
  #endif
  reset(2,top);
  
  //initial steps
  init_pmem("inst.bin");
  init_disasm("riscv64-pc-linux-gnu");
  init_regex();
  init_gpu();
  itrace = fopen("itrace.log","w");
  for (i = 0; i < argc; i++)
  {
    if (memcmp("elf",argv[i],3) == 0)
    {
      ftrace_enable = true;
      ftrace_infos = init_ftrace("inst_rom.elf",&ftrace_func_nums);
      flog_file = fopen("ftrace.log","w");
      fprintf(flog_file,"ftrace log\n");
    }
    if (memcmp("diff",argv[i],4) == 0)
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
  #ifdef waveform
  m_trace->close();
  #endif
  top->final();
  delete top;
  delete pmem;
  delete ftrace_infos;
  fclose(flog_file);
  //nvboard_quit();
  void print_mtrace_message();
  printf("HIT BAD TRAP\n");
  return 1;
}

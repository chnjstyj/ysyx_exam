#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <verilated.h>
#include <verilated_vcd_c.h>
//#include <nvboard.h>
#include <iostream>
#include <unistd.h>

#include "Vtop.h"

#include "svdpi.h"
#include "Vtop__Dpi.h"
#include "verilated_dpi.h"

#include <readline/readline.h>
#include <readline/history.h>

static VerilatedVcdC* m_trace = new VerilatedVcdC;
static Vtop* top = new Vtop;

static char* image_name = NULL;

#define MAX_SIM_TIME 100
vluint64_t sim_time = 0;

//void nvboard_bind_all_pins(Vtop *top);

uint32_t* memory = NULL;
uint64_t pc = io_inst_address;

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
  /*
  printf("Array Pointer is %x \n", svGetArrayPtr(r) ); 
	printf(" Lower index %d \n", svLow(r,1)); 
	printf(" Higher index %d \n", svHigh(r, 1) ); 
	printf(" Left index %d \n", svLeft(r,1)); 
	printf(" Right index %d \n", svRight(r, 1) ); 
	//printf(" Length of array %d \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\n", svLength(r,1) ); 
	printf(" Incremental %d \n",svIncrement(r,1)); 
	printf("Dimentions of Array %d \n", svDimensions(r)); 
	printf("Size of Array in bytes %d \n", svSizeOfArray(r) ); 
  */
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

static void single_cycle(Vtop* top)
{
  top->clock = 1;top->eval();
  sim_time++;
  m_trace->dump(sim_time);
  top->clock = 0;top->eval();
  sim_time++;
  m_trace->dump(sim_time);
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
    sim_time <= 200
    //1
    )
  {
    single_cycle(top);
    //nvboard_update();
  }

  m_trace->close();
  top->final();
  delete top;
  //nvboard_quit();
  printf("HIT BAD TRAP\n");
  return 1;
}

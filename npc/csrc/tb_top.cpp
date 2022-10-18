#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <verilated.h>
#include <verilated_vcd_c.h>
#include <nvboard.h>
#include <iostream>
#include <unistd.h>

#include "Vtop.h"

static Vtop top;
static VerilatedVcdC m_trace;

#define MAX_SIM_TIME 100
vluint64_t sim_time = 0;

void nvboard_bind_all_pins(Vtop *top);

static void single_cycle()
{
  top.clock = 1;top.eval();
  sim_time++;
  //m_trace.dump(sim_time);
  top.clock = 0;top.eval();
  sim_time++;
  //m_trace.dump(sim_time);
}

int main()
{

  //Verilated::traceEverOn(true);
  // VerilatedVcdC *m_trace = new VerilatedVcdC;
  //(&top)->trace(&m_trace, 5);
  //m_trace.open("waveform.vcd");

  nvboard_bind_all_pins(&top);
  nvboard_init();
  top.reset = 1;
  top.clock = 0;top.eval();
  top.clock = 1;top.eval();
  top.reset = 0;top.eval();
  top.clock = 0;top.eval();
  while (1)
  {
    // usleep(10000);
    single_cycle();
    
    if (top.io_data !=0) 
    {
      //printf("%x\n",top.io_data);
    }
    nvboard_update();
  }
  // m_trace.close();
  nvboard_quit();
  return 0;
}

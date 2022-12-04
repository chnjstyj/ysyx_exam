#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <verilated.h>
#include <verilated_vcd_c.h>
//#include <nvboard.h>
#include <iostream>
#include <unistd.h>

#include "Vtop.h"

static VerilatedVcdC m_trace;

#define MAX_SIM_TIME 100
vluint64_t sim_time = 0;

//void nvboard_bind_all_pins(Vtop *top);

static void single_cycle(Vtop* top)
{
  top->clock = 1;top->eval();
  sim_time++;
  m_trace.dump(sim_time);
  top->clock = 0;top->eval();
  sim_time++;
  m_trace.dump(sim_time);
}

static void reset(int n,Vtop* top) {
  top->reset = 1;
  while (n -- > 0) single_cycle(top);
  top->reset = 0;
}

int main()
{
  Vtop *top = new Vtop;
  Verilated::traceEverOn(true);
  //VerilatedVcdC *m_trace = new VerilatedVcdC;
  top->trace(&m_trace, 10);
  m_trace.open("waveform.vcd");

  //nvboard_bind_all_pins(&top);
  //nvboard_init();
  reset(10,top);
  while (
    sim_time <= 200
    //1
    )
  {
    single_cycle(top);
    //nvboard_update();
  }
  m_trace.close();
  delete top;
  //nvboard_quit();
  return 0;
}

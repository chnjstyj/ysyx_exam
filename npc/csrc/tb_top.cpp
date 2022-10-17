#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <verilated.h>
#include <verilated_vcd_c.h>
#include <nvboard.h>
#include <iostream>

#include "Vtop.h"

static Vtop top;

#define MAX_SIM_TIME 20
vluint64_t sim_time = 0;

void nvboard_bind_all_pins(Vtop* top);

static void single_cycle() {
  top.clock = 0; top.eval();
  top.clock = 1; top.eval();
}

int main()
{
    /*
    Verilated::traceEverOn(true);
    VerilatedVcdC *m_trace = new VerilatedVcdC;
    (&top)->trace(m_trace, 5);
    m_trace->open("waveform.vcd");*/
    nvboard_bind_all_pins(&top);
    nvboard_init();
    top.reset = 1;
    top.clock = 0; top.eval();
    top.clock = 1; top.eval();
    top.reset = 0;
    while(1)
    {
        single_cycle();
        nvboard_update();
    }
    nvboard_quit();
    return 0;
}

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <verilated.h>
#include <verilated_vcd_c.h>

#include "Vtop.h"

#define MAX_SIM_TIME 20
vluint64_t sim_time = 0;

int main()
{
    Vtop* top = new Vtop;

    Verilated::traceEverOn(true);
    VerilatedVcdC *m_trace = new VerilatedVcdC;
    top->trace(m_trace, 5);
    m_trace->open("waveform.vcd");
    while (sim_time < 20)
    {
        int a = rand()&1;
        int b = rand()&1;
        top -> a = a;
        top -> b = b;
        top->eval();
        m_trace->dump(sim_time);
        printf("a = %d, b = %d, f = %d\n", a, b, top->f);
        assert(top->f == (a ^ b));
        sim_time++;
    }

    m_trace->close();
    delete top;
    return 0;
}
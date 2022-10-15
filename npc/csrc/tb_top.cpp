#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <verilated.h>
#include <verilated_vcd_c.h>
#include <nvboard.h>

#include "Vtop.h" 

static Vtop top;

#define MAX_SIM_TIME 20
vluint64_t sim_time = 0;

void nvboard_bind_all_pins(Vtop* top);

int main()
{
    //Vtop *top = new Vtop;
    nvboard_bind_all_pins(&top);
    nvboard_init();
    while(1)
    {
        nvboard_update();
    }
    return 0;
}
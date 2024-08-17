#include <stdio.h>
#include <sys/time.h>
#include <stdint.h>
#include "NDL.h"

int main()
{
    uint32_t time = 0;
    uint32_t i = 0;
    struct timeval time_ = {0};
    NDL_Init(0);
    printf("start timer test\n");
    while (1)
    {
        time = NDL_GetTicks();
        //printf("time passed %d\n",time);
        if (time - i * 500 >= 500)
        {
            i++;
            printf("time has passed 0.5 sec NDL version %d\n",time);
        }
    }
    return 1;
}
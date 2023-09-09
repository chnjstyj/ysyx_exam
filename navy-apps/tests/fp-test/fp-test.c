#include "fixedptc.h"
#include <stdio.h>

fixedpt tests[] = {1.1 * 256,9 * 256,256.3 * 256,322.6 * 256,0,-1.2 * 256,-9.9 * 256};
int ceil_[] = {2,9,257,323,0,-1,-9};
int floor_[] = {1,9,256,322,0,-2,-10};

int main()
{
    int i;
    for (i = 0; i < 7; i++)
    {
        if (ceil_[i] != ((int)fixedpt_ceil(tests[i]) / 256))
            printf("different ceil : %d  %d\n",ceil_[i],((int)fixedpt_ceil(tests[i]) / 256));
    }
    for (i = 0; i < 7; i++)
    {
        if (floor_[i] != ((int)fixedpt_floor(tests[i]) / 256))
            printf("different floor : %d  %d\n",floor_[i],((int)fixedpt_floor(tests[i]) / 256));
    }
    printf("end\n");
    return 0;
}
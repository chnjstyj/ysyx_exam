#include "mtrace.h"
#include <string.h>
#include <stdio.h>

mtrace_info mtrace[50] = {0};
int mtrace_head = 0;
void print_mtrace_message()
{
  int end_point = mtrace_head;
  printf("Mode\tAddr\n");
  do 
  {
    if (mtrace_head - 1 < 0)
    {
      mtrace_head = 49;
    }
    else 
    {
      mtrace_head--;
    }
    printf("%s\t0x%x\n",mtrace[mtrace_head].mode,mtrace[mtrace_head].addr);
  }
  while (mtrace_head != end_point);
}

void update_mtrace(const char* mode,uint64_t addr)
{
    if (mtrace_head != 49)
    {
        strcpy(mtrace[mtrace_head].mode,mode);
        mtrace[mtrace_head].addr = addr;
        mtrace_head++;
    }
    else 
    {
        mtrace_head = 0;
        strcpy(mtrace[mtrace_head].mode,mode);
        mtrace[mtrace_head].addr = addr;
        mtrace_head++;
    }
}
#include <stdint.h>

void print_mtrace_message();
void update_mtrace(const char* mode,uint64_t addr);

typedef struct 
{
  char mode[6];
  uint64_t addr;
}mtrace_info;
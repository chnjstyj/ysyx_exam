#include <am.h>
#include <riscv/riscv.h>
#include <klib-macros.h>

#define UART16550 0x10000000

extern char _heap_start;
int main(const char *args);

extern char _pmem_start;
#define PMEM_SIZE (128 * 1024 * 1024)
#define PMEM_END  ((uintptr_t)&_pmem_start + PMEM_SIZE)

#define HEAP_SIZE 8*1024
Area heap = RANGE(&_heap_start, &_heap_start+HEAP_SIZE);
#ifndef MAINARGS
#define MAINARGS ""
#endif
static const char mainargs[] = MAINARGS;

# define npc_trap(code) asm volatile("mv a0, %0; ebreak" : :"r"(code))

void putch(char ch) {
  outb(UART16550, ch);
}

void halt(int code) {
  npc_trap(code);
  
  // should not reach here
  while (1);
}

void _trm_init() {
  int ret = main(mainargs);
  halt(ret);
}

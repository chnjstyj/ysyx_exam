#include <am.h>
#include <stdio.h>
#include <cassert>

Area heap;

void putch(char ch) {
    putchar(ch);
}

void halt(int code) {
  assert(code);

  // should not reach here
  while (1);
}

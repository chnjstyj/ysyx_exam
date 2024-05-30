#include <am.h>
#include <riscv/riscv.h>
#define KEYDOWN_MASK 0x8000

void __am_input_keybrd(AM_INPUT_KEYBRD_T *kbd) {
  kbd->keycode = inl(KBD_ADDR);
  if ((kbd->keycode & KEYDOWN_MASK) != 0)
  {
    kbd->keycode &= 0x7fff;
    kbd->keydown = 1;
  }
  else 
  {
    kbd->keycode &= 0x7fff;
    kbd->keydown = 0;
  }
}

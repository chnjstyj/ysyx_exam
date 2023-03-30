#include <am.h>
#include <nemu.h>
#include <stdio.h>

#define KEYDOWN_MASK 0x8000

void __am_input_keybrd(AM_INPUT_KEYBRD_T *kbd) {
  kbd->keycode = inl(KBD_ADDR);
  if (kbd->keycode != 0)  printf("test %d\n",kbd->keycode);
  if (kbd->keycode != AM_KEY_NONE)
  {
    kbd->keydown = 1;
    printf("test %d\n",kbd->keycode);
  }
  else 
  {
    kbd->keydown = 0;
  }
}

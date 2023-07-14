#include <am.h>
#include <stdio.h>
#include <SDL.h>
#include <sdl-event.h>

#define KEYDOWN_MASK 0x8000

void __am_input_keybrd(AM_INPUT_KEYBRD_T *kbd) {
  SDL_Event ev = {0};
  SDL_PollEvent(&ev);
  if(ev.type == SDL_KEYDOWN)
  {
    kbd->keydown = 1;
    kbd->keycode = ev.key.keysym.sym;
  }
  else if (ev.type == SDL_KEYUP)
  {
    kbd->keydown = 0;
    kbd->keycode = ev.key.keysym.sym;
  }
}

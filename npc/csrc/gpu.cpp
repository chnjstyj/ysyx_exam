#include "gpu.h"
#include <stdint.h>
#include <SDL2/SDL.h>

void *vmem = NULL;
static SDL_Renderer *renderer = NULL;
static SDL_Texture *texture = NULL;

uint8_t vgasync = 0;

inline uint32_t screen_size() {
  return 400 *300 * sizeof(uint32_t);
}

void init_gpu()
{
  SDL_Window *window = NULL;
  char title[128];
  sprintf(title, "%s-NPC", "riscv64");
  SDL_Init(SDL_INIT_VIDEO);
  SDL_CreateWindowAndRenderer(
      SCREEN_W * 2,
      SCREEN_H * 2,
      0, &window, &renderer);
  SDL_SetWindowTitle(window, title);
  texture = SDL_CreateTexture(renderer, SDL_PIXELFORMAT_ARGB8888,
      SDL_TEXTUREACCESS_STATIC, SCREEN_W, SCREEN_H);

  vmem = (uint32_t*)malloc(screen_size());
  memset(vmem, 0, screen_size());
}

inline void update_screen() {
  SDL_UpdateTexture(texture, NULL, vmem, SCREEN_W * sizeof(uint32_t));
  SDL_RenderClear(renderer);
  SDL_RenderCopy(renderer, texture, NULL, NULL);
  SDL_RenderPresent(renderer);
}

void vga_update_screen() {
  if (vgasync)
  {
    update_screen();
    vgasync = 0;
  }
}
#ifndef GPU_HEAD
#define GPU_HEAD

#define SCREEN_W 400
#define SCREEN_H 300

#include <stdint.h>
#include <SDL2/SDL.h>

SDL_Window* init_gpu();
void vga_update_screen();

extern uint8_t vgasync;
extern void *vmem;

#endif 
#include <am.h>
#include <riscv/riscv.h>
#include <stdio.h>

#define SYNC_ADDR (VGACTL_ADDR + 8)
static int w;
static int h;

void __am_gpu_init() {
  uint32_t size = inl(VGACTL_ADDR);
  w = (size >> 16);  // TODO: get the correct width
  h = (size & 0xffff);  // TODO: get the correct height
}

void __am_gpu_config(AM_GPU_CONFIG_T *cfg) {
  *cfg = (AM_GPU_CONFIG_T) {
    .present = true, .has_accel = false,
    .width = 0, .height = 0,
    .vmemsz = 0
  };
  uint32_t size = inl(VGACTL_ADDR);
  cfg->height = (size & 0xffff);
  cfg->width = (size >> 16);
  //printf("h %d w %d\n",cfg->height,cfg->width);
}

void __am_gpu_fbdraw(AM_GPU_FBDRAW_T *ctl) {
  if (ctl->pixels != NULL)
  {
    uint32_t *fb = (uint32_t *)(uintptr_t)FB_ADDR;
    uint32_t *p = (uint32_t *)(uintptr_t)ctl->pixels;
    int i,j;
    int k,l;
    k = 0;l = 0;
    for (i = ctl->y; i < ctl->h + ctl->y; i++)
    {
      for (j = ctl->x; j < ctl->w + ctl->x; j++)
      {
        if (i < h && j < w) 
        {
          fb[i * w + j] = p[k * ctl->w + l];
          l++;
        }
      }
      k++;
      l = 0;
    }
  }
  if (ctl->sync) {
    outl(SYNC_ADDR, 1);
  }
}

void __am_gpu_status(AM_GPU_STATUS_T *status) {
  status->ready = true;
}

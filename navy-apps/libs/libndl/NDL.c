#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

static int evtdev = -1;
static int fbdev = -1;
static int screen_w = 0, screen_h = 0;
struct timeval setup_time = {0};
int key_fd;
int gpu_config;
int gpu_fb;

uint32_t NDL_GetTicks() {
  struct timeval time_now = {0};
  gettimeofday(&time_now,NULL);
  return time_now.tv_usec - setup_time.tv_usec;
}

int NDL_PollEvent(char *buf, int len) {
  int r = read(key_fd,buf,len);
  if ( r != -1)
  {
    return 1;
  }
  else 
  return 0;
}

uint32_t * canvas = NULL;
int canvas_h;
void NDL_OpenCanvas(int *w, int *h) {
  if (getenv("NWM_APP")) {
    int fbctl = 4;
    fbdev = 5;
    screen_w = *w; screen_h = *h;
    char buf[64];
    int len = sprintf(buf, "%d %d", screen_w, screen_h);
    // let NWM resize the window and create the frame buffer
    write(fbctl, buf, len);
    while (1) {
      // 3 = evtdev
      int nread = read(3, buf, sizeof(buf) - 1);
      if (nread <= 0) continue;
      buf[nread] = '\0';
      if (strcmp(buf, "mmap ok") == 0) break;
    }
    close(fbctl);
  }
  char str[50] = {0};
  int i = read(gpu_config,str,50);
  sscanf(str,"WIDTH:%d\nHEIGHT:%d\n",&screen_w,&screen_h);
  if (*h != 0 && *w != 0)
  {
    canvas = (uint32_t*)malloc((screen_w) * (*h) * sizeof(uint32_t));
    canvas_h = *h;
    printf("Canvas Width : %d\nCanvas Height : %d\n",*w,*h);
  }
  else 
  {    
    canvas = (uint32_t*)malloc((screen_w) * (screen_h) * sizeof(uint32_t));
    canvas_h = screen_h;
    printf("Canvas Width : %d\nCanvas Height : %d\n",screen_w,screen_h);
  }
}

void NDL_DrawRect(uint32_t *pixels, int x, int y, int w, int h) {
  
  int i,j,k = 0;
  for (i = y; i < y + h; i++)
  {
    lseek(gpu_fb,(i * screen_w) + x,SEEK_SET);
    k += write(gpu_fb,pixels + k,w);
  }
  /*
  int i,j,k = 0;
  lseek(gpu_fb,0,SEEK_SET);
  for (i = y;i < canvas_h; i++)
  {
    for (j = x; j < screen_w; j++)
    {
      if (j >= x && j < x + w)
      {
        *(canvas + i*screen_w + j) = *(pixels + i*w + j);
      }
    }
  }
  write(gpu_fb,canvas,screen_w * canvas_h);*/
}

void NDL_OpenAudio(int freq, int channels, int samples) {
}

void NDL_CloseAudio() {
}

int NDL_PlayAudio(void *buf, int len) {
  return 0;
}

int NDL_QueryAudio() {
  return 0;
}

int NDL_Init(uint32_t flags) {
  if (getenv("NWM_APP")) {
    evtdev = 3;
  }
  //system setup time
  gettimeofday(&setup_time,NULL);
  key_fd = open("/dev/events",0,0);
  gpu_config = open("/proc/dispinfo",0,0);
  gpu_fb = open("/dev/fb",0,0);
  return 0;
}

void NDL_Quit() {
}

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/time.h>

static int evtdev = -1;
static int fbdev = -1;
static int screen_w = 0, screen_h = 0;
struct timeval setup_time = {0};
int key_fd;
int gpu_config;
int gpu_fb;
int audio_sb;
int audio_config;

uint32_t NDL_GetTicks() {
  struct timeval time_now = {0};
  gettimeofday(&time_now,NULL);
  return (time_now.tv_usec - setup_time.tv_usec)/1000;
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
int canvas_w;
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
  if (canvas != NULL) free(canvas);
  if (*h != 0 && *w != 0)
  {
    canvas = (uint32_t*)malloc((*w) * (*h) * sizeof(uint32_t));
    canvas_h = *h;
    canvas_w = *w;
    printf("Canvas Width : %d\nCanvas Height : %d\n",*w,*h);
  }
  else 
  {    
    canvas = (uint32_t*)malloc((screen_w) * (screen_h) * sizeof(uint32_t));
    canvas_w = screen_w;
    canvas_h = screen_h;
    printf("Canvas Width : %d\nCanvas Height : %d\n",screen_w,screen_h);
    *w = screen_w;
    *h = screen_h;
  }
}

void NDL_DrawRect(uint32_t *pixels, int x, int y, int w, int h) {
  /*
  int i,j,k = 0;
  for (i = y; i < y + h; i++)
  {
    lseek(gpu_fb,(i * screen_w) + x,SEEK_SET);
    k += write(gpu_fb,pixels + k,w);
  }*/
  /*
  int i,j,k = 0;
  for (i = y;i < y + h; i++)
  {
    for (j = x; j < x + w; j++)
    {
      if (i >= 0 && j >= 0 && i < canvas_h && j < canvas_w)
      {
        *(canvas + i * canvas_w + j) = *(pixels + i * canvas_w + j);
      }
      k++;
    }
  }
  lseek(gpu_fb,canvas_w,SEEK_SET);
  write(gpu_fb,canvas,canvas_w * canvas_h); */
  
  int i,j,k = 0;
  uint64_t sum = y * canvas_w;
  uint64_t sum2 = y * screen_w;
  int length = 0;
  for (i = y;i < y + h; i++)
  {
    // for (j = x; j < x + w; j++)
    // {
    //   if (i >= 0 && j >= 0 && i < canvas_h && j < canvas_w)
    //   {
    //     *(canvas + sum + j) = *(pixels + sum + j);
    //     //printf("%x\n",*(canvas + i * canvas_w + j));
    //     k++;
    //   }
    // }
    j = x;
    if (j >= 0)
    {
      if (j + w > canvas_w)
      {
        length = canvas_w - j;
      }
      else 
      {
        length = w;
      }
    }
    else 
    {
      if (j + w > canvas_w)
      {
        length = canvas_w;
      }
      else 
      {
        length = w ;
      }
    }
    memcpy(canvas + sum,pixels + sum,length * sizeof(uint32_t));
    lseek(gpu_fb,sum2,SEEK_SET);  
    write(gpu_fb,canvas + sum,canvas_w); 
    sum += canvas_w;
    sum2 += screen_w;
    k = 0;
  }
}

void NDL_OpenAudio(int freq, int channels, int samples) {
  audio_config = open("/dev/sbctl",0,0);
  audio_sb = open("/dev/sb",0,0); 
  int buf[3] = {freq,channels,samples};
  write(audio_config,buf,3 * sizeof(int));
}

void NDL_CloseAudio() {
  close(audio_config);
  close(audio_sb);
}

int NDL_PlayAudio(void *buf, int len) {
  return write(audio_sb,buf,len);
}

int NDL_QueryAudio() {
  int length;
  read(audio_config,&length,sizeof(int));
  return length;
}

int NDL_Init(uint32_t flags) {
  if (getenv("NWM_APP")) {
    evtdev = 3;
  }
  //system setup time
  gettimeofday(&setup_time,NULL);
  key_fd = open("/dev/events",0,0);
  gpu_config = open("/dev/dispinfo",0,0);
  gpu_fb = open("/dev/fb",0,0);
  return 0;
}

void NDL_Quit() {
}

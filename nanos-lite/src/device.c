#include <common.h>

#if defined(MULTIPROGRAM) && !defined(TIME_SHARING)
# define MULTIPROGRAM_YIELD() yield()
#else
# define MULTIPROGRAM_YIELD()
#endif

#define NAME(key) \
  [AM_KEY_##key] = #key,

static const char *keyname[256] __attribute__((used)) = {
  [AM_KEY_NONE] = "NONE",
  AM_KEYS(NAME)
};

size_t serial_write(const void *buf, size_t offset, size_t len) {
  int i = 0;
  for (i = 0; i < len; i ++)
  {
    //printf("\nt:%c\n",*((char*)buf + i));
    putch(*((char*)buf + i));
  }
  return i;
}

size_t events_read(void *buf, size_t offset, size_t len) {
  int i = 0;
  AM_INPUT_KEYBRD_T ev = io_read(AM_INPUT_KEYBRD);
  if(ev.keycode == AM_KEY_NONE)
  {
    return -1;
  }
  else 
  {
    if (ev.keydown == 1)
    {
      strncpy(buf,"kd ",3);
    }
    else
    {
      strncpy(buf,"ku ",3);
    }
    i += 3;
    strcpy(buf+3,keyname[ev.keycode]);
    i += strlen(keyname[ev.keycode]);
  }
  return i;
}

size_t dispinfo_read(void *buf, size_t offset, size_t len) {
  //int i;
  AM_GPU_CONFIG_T t = io_read(AM_GPU_CONFIG);
  char* str = (char *)malloc(50);
  sprintf(str,"WIDTH:%d\nHEIGHT:%d\n",t.width,t.height);
  strncpy(buf,str,len);
  free(str);
  return len;
}

extern uint32_t* fb;
size_t fb_write(const void *buf, size_t offset, size_t len) {
  AM_GPU_CONFIG_T t = io_read(AM_GPU_CONFIG);
  //offset = canvas_w
  int canvas_w = offset;
  int canvas_h;
  if (canvas_w != 0)
    canvas_h = (int)len / (int)canvas_w;
  else
    canvas_h = 0; 
  int i,j,k = 0;
  for (i = 0;i < canvas_h; i++)
  {
    for (j = 0; j < canvas_w; j++)
    {
      *(fb + i * t.width + j) = *((uint32_t *)buf + k);
      k++;
    }
  }
  io_write(AM_GPU_FBDRAW,0,0,fb,t.width,t.height,true);
  return i;
}

void init_device() {
  Log("Initializing devices...");
  ioe_init();
}

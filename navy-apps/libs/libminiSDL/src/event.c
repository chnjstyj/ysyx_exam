#include <NDL.h>
#include <SDL.h>

#define keyname(k) #k,

static const char *keyname[] = {
  "NONE",
  _KEYS(keyname)
};

int SDL_PushEvent(SDL_Event *ev) {
  return 0;
}

int SDL_PollEvent(SDL_Event *ev) {
  int i;
  char str[64] = {0};
  //CallbackHelper();
  if (NDL_PollEvent(str,sizeof(str)) == 1)
  {
    if (strncmp(str,"kd",2) == 0)
    {
      ev->type = SDL_KEYDOWN;
      for (i = 0; i < sizeof(keyname) / sizeof(keyname[0]); i++)
      {
        if (strcmp(str + 3,keyname[i]) == 0)
        {
          (ev->key).keysym.sym = i;
          states[i] = 1;
          return 1;
        }
      }
    }
    else if (strncmp(str,"ku",2) == 0)
    {
      ev->type = SDL_KEYUP;
      for (i = 0; i < sizeof(keyname) / sizeof(keyname[0]); i++)
      {
        if (strcmp(str + 3,keyname[i]) == 0)
        {
          (ev->key).keysym.sym = i;
          states[i] = 0;
          return 1;
        }
      }
    }
  }
  return 0;
}

int SDL_WaitEvent(SDL_Event *event) {
  return 1;
}

int SDL_PeepEvents(SDL_Event *ev, int numevents, int action, uint32_t mask) {
  return 0;
}

uint8_t* SDL_GetKeyState(int *numkeys) {
  return NULL;
}

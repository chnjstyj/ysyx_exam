#include <NDL.h>
#include <SDL.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#define keyname(k) #k,

static const char *keyname[] = {
  "NONE",
  _KEYS(keyname)
};

uint8_t states[sizeof(keyname) / sizeof(keyname[0])] = {0};

int SDL_PushEvent(SDL_Event *ev) {
  printf("not impleted\n");
  assert(0);
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
  int i;
  char str[64] = {0};
  while (NDL_PollEvent(str,sizeof(str)) == 0)
  {
    printf("test wait event\n");
  }

  if (strncmp(str,"kd",2) == 0)
  {
    event->type = SDL_KEYDOWN;
    for (i = 0; i < sizeof(keyname) / sizeof(keyname[0]); i++)
    {
      if (strcmp(str + 3,keyname[i]) == 0)
      {
        (event->key).keysym.sym = i;
        return 1;
      }
    }
  }
  else if (strncmp(str,"ku",2) == 0)
  {
    event->type = SDL_KEYUP;
    for (i = 0; i < sizeof(keyname) / sizeof(keyname[0]); i++)
    {
      if (strcmp(str + 3,keyname[i]) == 0)
      {
        (event->key).keysym.sym = i;
        return 1;
      }
    }
  }
  return 1;
}

int SDL_PeepEvents(SDL_Event *ev, int numevents, int action, uint32_t mask) {
  printf("not impleted\n");
  assert(0);
  return 0;
}

uint8_t* SDL_GetKeyState(int *numkeys) {
  //TODO 
  return states;
}

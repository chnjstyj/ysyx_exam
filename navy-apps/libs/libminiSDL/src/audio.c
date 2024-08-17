#include <NDL.h>
#include <SDL.h>
#include <stdlib.h>

#define CONFIG_SB_SIZE 0x10000

SDL_AudioSpec* s = NULL;
uint8_t *stream = NULL;
static uint32_t before;
static int free_;

void CallbackHelper()
{
  uint32_t now = SDL_GetTicks();
  if (now - before >= 10000 && stream != NULL)
  {
    free_ = NDL_QueryAudio(); //free space to write
    s->callback(NULL,stream,free_);
    before = now;
  }
}

int SDL_OpenAudio(SDL_AudioSpec *desired, SDL_AudioSpec *obtained) {
  /*
  NDL_OpenAudio(desired->freq,desired->channels,desired->samples);
  if (obtained != NULL)
  {
    obtained->freq = desired->freq;
    obtained->channels = desired->channels;
    obtained->samples = desired->samples;
    obtained->format = desired->format;
    obtained->callback = desired->callback;
    obtained->size = desired->size;
    obtained->userdata = desired->userdata;
  }
  s = desired;
  stream = (uint8_t*)malloc(CONFIG_SB_SIZE * sizeof(uint8_t*));
  return 0;*/
  return 0;
}

void SDL_CloseAudio() {
  free(stream);
}

void SDL_PauseAudio(int pause_on) {
  if (!pause_on)
  {
    //start to play
    NDL_PlayAudio(stream,free_);
    return ;
  }
  else
  {
    return ;
  }
}

void SDL_MixAudio(uint8_t *dst, uint8_t *src, uint32_t len, int volume) {
}

SDL_AudioSpec *SDL_LoadWAV(const char *file, SDL_AudioSpec *spec, uint8_t **audio_buf, uint32_t *audio_len) {
  return NULL;
}

void SDL_FreeWAV(uint8_t *audio_buf) {
}

void SDL_LockAudio() {
}

void SDL_UnlockAudio() {
}

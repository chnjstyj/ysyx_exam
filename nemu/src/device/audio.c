/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <common.h>
#include <device/map.h>
#include <SDL2/SDL.h>

enum {
  reg_freq,
  reg_channels,
  reg_samples,//8
  reg_sbuf_size,//12
  reg_init,//16
  reg_count,
  nr_reg
};

static uint8_t *sbuf = NULL;
static uint32_t *audio_base = NULL;
uint32_t base_addr = 0;

//len : 剩余需要填充的数量
void MyAudioCallback(void* userdata,Uint8* stream,int len)
{
  if (audio_base[reg_count] > 0)
  {
    //int i = 0;
    int slen = audio_base[reg_count];
    len = (len > slen ? slen : len);
    SDL_MixAudio(stream,sbuf,len,SDL_MIX_MAXVOLUME);
    memcpy(sbuf,sbuf+len,slen-len);
    audio_base[reg_count] -= len;
    //printf("count %d addr %d\n",audio_base[reg_count],base_addr);
    /*
    while (slen > 0)
    {
      *(stream + len + i) = *(sbuf + i);
      i++;
      slen--;
    }
    SDL_PauseAudio(0);*/
    //SDL_CloseAudio();
  }
  else 
  {
    SDL_PauseAudio(1);
  }
}

SDL_AudioSpec s = {};
static void audio_io_handler(uint32_t offset, int len, bool is_write) {
  if (offset == 8)
  {
    s.format = AUDIO_S16SYS;  // 假设系统中音频数据的格式总是使用16位有符号数来表示
    s.userdata = NULL;        // 不使用
    s.freq = audio_base[reg_freq];
    s.channels = audio_base[reg_channels];
    s.samples = audio_base[reg_samples];
    s.callback = MyAudioCallback;
  }
  if (offset == 16 && audio_base[reg_init] == 1)
  {
    SDL_InitSubSystem(SDL_INIT_AUDIO);
    if (SDL_OpenAudio(&s, NULL) < 0)
    {
      assert(0);
    }
    SDL_PauseAudio(0);
  }
}

void init_audio() {
  uint32_t space_size = sizeof(uint32_t) * nr_reg;//24
  audio_base = (uint32_t *)new_space(space_size);
  audio_base[reg_sbuf_size] = CONFIG_SB_SIZE;
#ifdef CONFIG_HAS_PORT_IO
  add_pio_map ("audio", CONFIG_AUDIO_CTL_PORT, audio_base, space_size, audio_io_handler);
#else
  add_mmio_map("audio", CONFIG_AUDIO_CTL_MMIO, audio_base, space_size, audio_io_handler);
#endif

  sbuf = (uint8_t *)new_space(CONFIG_SB_SIZE);
  add_mmio_map("audio-sbuf", CONFIG_SB_ADDR, sbuf, CONFIG_SB_SIZE, NULL);
}

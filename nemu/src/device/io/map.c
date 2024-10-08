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

#include <isa.h>
#include <memory/host.h>
#include <memory/vaddr.h>
#include <device/map.h>

#define IO_SPACE_MAX (2 * 1024 * 1024)

static uint8_t *io_space = NULL;
static uint8_t *p_space = NULL;

uint8_t* new_space(int size) {
  uint8_t *p = p_space;
  // page aligned;
  size = (size + (PAGE_SIZE - 1)) & ~PAGE_MASK;
  p_space += size;
  assert(p_space - io_space < IO_SPACE_MAX);
  return p;
}

#ifdef CONFIG_DTRACE
static dtrace_info dtrace[20];
static int dtrace_head;
void update_dtrace(int direction,const char* device_name,uint64_t addr)
{
  int length = sizeof(device_name);
  //read
  if (dtrace_head == 19)
  {
    dtrace_head = 0;
  }
  if (!direction)
  {
    strcpy(dtrace[dtrace_head].mode,"read");
  }
  else //write
  {  
    strcpy(dtrace[dtrace_head].mode,"write");
  }
  if (dtrace[dtrace_head].device_name != NULL) free(dtrace[dtrace_head].device_name);
  dtrace[dtrace_head].device_name = (char*)malloc(length);
  memcpy(dtrace[dtrace_head].device_name,device_name,length);
  dtrace[dtrace_head].addr = addr;
  dtrace_head++;
}
#endif
extern void print_mtrace_message();

static void check_bound(IOMap *map, paddr_t addr) {
  if (map == NULL) {
    Assert(map != NULL, "address (" FMT_PADDR ") is out of bound at pc = " FMT_WORD, addr, cpu.pc);
  } else {
    Assert(addr <= map->high && addr >= map->low,
        "address (" FMT_PADDR ") is out of bound {%s} [" FMT_PADDR ", " FMT_PADDR "] at pc = " FMT_WORD,
        addr, map->name, map->low, map->high, cpu.pc);
  }
}

static void invoke_callback(io_callback_t c, paddr_t offset, int len, bool is_write) {
  if (c != NULL) { c(offset, len, is_write); }
}

void init_map() {
  io_space = malloc(IO_SPACE_MAX);
  assert(io_space);
  p_space = io_space;
}

word_t map_read(paddr_t addr, int len, IOMap *map) {
  assert(len >= 1 && len <= 8);
  check_bound(map, addr);
  paddr_t offset = addr - map->low;
  #ifdef CONFIG_DTRACE
  update_dtrace(0,map->name,addr);
  //printf("dtrace:%s %s %x\n",dtrace[(dtrace_head-1)].mode,dtrace[(dtrace_head-1)].device_name,dtrace[(dtrace_head-1)].addr);
  #endif
  invoke_callback(map->callback, offset, len, false); // prepare data to read
  word_t ret = host_read(map->space + offset, len);
  return ret;
}

void map_write(paddr_t addr, int len, word_t data, IOMap *map) {
  assert(len >= 1 && len <= 8);
  check_bound(map, addr);
  paddr_t offset = addr - map->low;
  #ifdef CONFIG_DTRACE
  update_dtrace(1,map->name,addr);
  #endif
  host_write(map->space + offset, len, data);
  invoke_callback(map->callback, offset, len, true);
}

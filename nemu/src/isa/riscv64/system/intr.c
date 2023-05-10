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
#include "../local-include/reg.h"

#ifdef CONFIG_ETRACE
static int eringbuf_head;
typedef struct etrace
{
  word_t NO;
  vaddr_t epc;
} etrace_info;

static etrace_info eringbuf[16];

void print_etrace()
{
  int i;
  for (i = 0; i < 16; i++)
  {
    if (eringbuf[i].epc != 0)
    {
      printf("etrace %d:NO:%ld\t%lx\n",i,eringbuf[i].NO,eringbuf[i].epc);
    }
  }
}

#endif

word_t isa_raise_intr(word_t NO, vaddr_t epc) {
  /* TODO: Trigger an interrupt/exception with ``NO''.
   * Then return the address of the interrupt/exception vector.
   */
  #ifdef CONFIG_ETRACE
  eringbuf[eringbuf_head].NO = NO;
  eringbuf[eringbuf_head].epc = epc;
  eringbuf_head++;
  #endif
  csr(mepc) = epc;
  csr(mcause) = NO;
  return csr(mtvec);
}

word_t isa_query_intr() {
  return INTR_EMPTY;
}

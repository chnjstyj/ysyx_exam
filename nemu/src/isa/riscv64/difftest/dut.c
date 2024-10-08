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
#include <cpu/difftest.h>
#include "../local-include/reg.h"

bool isa_difftest_checkregs(CPU_state *ref_r, vaddr_t pc) {
  int i = 0;
  int j;
  int result = true;
  while (i < 32)
  {
    if (ref_r->gpr[i] != cpu.gpr[i])
    {
      printf("different regs, npc:0x%lx\tref:0x%lx\n",cpu.gpr[i],ref_r->gpr[i]);
      for (j = 0; j < 32;j ++)
      {
        printf("%05d:0x%016lx\n",j,ref_r->gpr[j]);
      }
      return false;
    }
    i++;
  }

  if (cpu.pc != ref_r->pc)
  {
    printf("different pc, npc:0x%lx\tref:0x%lx\n",cpu.pc,ref_r->pc);
    return false;
  }

  for (i = 0; i < 4; i++)
  {
    if (cpu.csr[i] != ref_r->csr[i])
    {
      printf("different csr%d, npc:0x%lx\tref:0x%lx\n",i,cpu.csr[i],ref_r->csr[i]);
      result = false;
    }
  }
  
  if (result == false)
  {
    for (j = 0; j < 32;j ++)
    {
      printf("%05d:0x%016lx\n",j,ref_r->gpr[j]);
    }
    return false;
  }
  return true;
}

void isa_difftest_attach() {
}

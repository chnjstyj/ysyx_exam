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

#include "sdb.h"

#define NR_WP 32

static WP wp_pool[NR_WP] = {};
static WP *head = NULL, *free_ = NULL;

void init_wp_pool() {
  int i;
  for (i = 0; i < NR_WP; i ++) {
    wp_pool[i].NO = i;
    wp_pool[i].next = (i == NR_WP - 1 ? NULL : &wp_pool[i + 1]);
  }

  head = NULL;
  free_ = wp_pool;
}

/* TODO: Implement the functionality of watchpoint */
WP* new_wp(char* expr,uint64_t result)
{
  if (free_ != NULL)
  {
    strcpy(free_->expr,expr);
    free_->result_previous = result;
    WP* next_ = free_->next;
    free_->next = head;
    head = free_;
    free_ = next_;
    return head;
  }
  else 
  {
    assert(0);
    return 0;
  }
  
}

void free_wp(WP *wp)
{
  WP* s = head;
  WP* p = head;
  while (s != NULL)
  {
    if (s->NO == wp->NO) 
    {
      p->next = s->next;
      s->next = free_;
      free_ = s;
      return;
    }
    else
    {
      p = s;
      s = s->next;
    } 
  }
  printf("Watchpoint Not Found\n");
}

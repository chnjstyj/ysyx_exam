#include <common.h>
#include <stddef.h>
#include "syscall.h"

static int stracebuf_head;
//static int iringbuf_tail;
static strace_info stracebuf[16];

void init_strace()
{
  int i;
  for (i = 0; i < 16; i++)
  {
    stracebuf[i].syscall_id = -1;
  }
}

void update_strace(Context *c)
{
  if (stracebuf_head == 16)
  {
    stracebuf_head = 0;
  }
  stracebuf[stracebuf_head].syscall_id = c->GPR1;
  stracebuf[stracebuf_head].syscall_inputs[0] = c->GPR1;
  stracebuf[stracebuf_head].syscall_inputs[1] = c->GPR2;
  stracebuf[stracebuf_head].syscall_inputs[2] = c->GPR3;
  stracebuf[stracebuf_head].syscall_inputs[3] = c->GPR4;
  stracebuf[stracebuf_head].syscall_ret = c->GPRx;
  stracebuf_head ++;
}

int64_t write(int fd, const void *buf, size_t count);

void do_syscall(Context *c) {
  uintptr_t a[4];
  a[0] = c->GPR1;
  update_strace(c);
  //printf("syscall %d\n",a[0]);
  switch (a[0]) {
    case SYS_yield:yield(); break;
    case SYS_exit :halt(c->GPR2) ; break;
    case SYS_write:c->GPRx = write(c->GPR2,(void *)c->GPR3,c->GPR4); break;
    default: panic("Unhandled syscall ID = %d", a[0]);
  }
}

int64_t write(int fd, const void *buf, size_t count)
{
  int i = 0;
  //printf("\nnum:%d\n",count);
  if (fd == 1 || fd == 2)
  {
    for (i = 0; i < count; i ++)
    {
      //printf("\nt:%c\n",*((char*)buf + i));
      putch(*((char*)buf + i));
    }
  }
  return i;
}
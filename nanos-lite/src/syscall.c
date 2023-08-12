#include <common.h>
#include <stddef.h>
#include "syscall.h"
#include "fs.h"
#include <sys/time.h>
#include "proc.h"

typedef struct 
{
  int syscall_id;
  int syscall_inputs[4];
  int syscall_ret;
}strace_info;

static int stracebuf_head;
//static int iringbuf_tail;
static strace_info stracebuf[16];
void * pb = NULL;

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
int _gettimeofday(struct timeval *tv, struct timezone *tz);
void naive_uload(PCB *pcb, const char *filename);

void do_syscall(Context *c) {
  uintptr_t a[4];
  a[0] = c->GPR1;
  update_strace(c);
  //printf("syscall %d\n",a[0]);
  switch (a[0]) {
    case SYS_yield:yield(); break;
    case SYS_exit :/*halt(c->GPR2);*/ naive_uload(NULL,"/bin/menu");break;
    case SYS_write:c->GPRx = write(c->GPR2,(void *)c->GPR3,c->GPR4); break;
    case SYS_brk:pb = (void *)c->GPR2;c->GPRx = 0;break;
    case SYS_open:c->GPRx = fs_open((const char*)c->GPR2,c->GPR3,c->GPR4);break;
    case SYS_read:c->GPRx = fs_read(c->GPR2,(void *)c->GPR3,c->GPR4);break;
    case SYS_lseek:c->GPRx = fs_lseek(c->GPR2,c->GPR3,c->GPR4);break;
    case SYS_close:c->GPRx = fs_close(c->GPR2);break;
    case SYS_gettimeofday:c->GPRx = _gettimeofday((struct timeval *)c->GPR2,(struct timezone *)c->GPR3);break;
    case SYS_execve:naive_uload(NULL,(const char *)c->GPR2);
    default: panic("Unhandled syscall ID = %d", a[0]);
  }
}

int _gettimeofday(struct timeval *tv, struct timezone *tz)
{
  tv->tv_usec = io_read(AM_TIMER_UPTIME).us;
  return 0;
}

int64_t write(int fd, const void *buf, size_t count)
{
  int i = 0;
  i = fs_write(fd,buf,count);
  return i;
}
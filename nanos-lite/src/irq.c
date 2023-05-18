#include <common.h>
void do_syscall(Context *c);
static Context* do_event(Event e, Context* c) {
  //printf("do event %d\n",e.event);
  switch (e.event) {
    case EVENT_YIELD: Log("yield detected\n");break;
    case EVENT_SYSCALL: do_syscall(c); break;
    default:  do_syscall(c); break;//panic("Unhandled event ID = %d", e.event);
  }

  return c;
}

void init_irq(void) {
  Log("Initializing interrupt/exception handler...");
  cte_init(do_event);
}

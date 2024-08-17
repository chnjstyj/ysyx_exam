#include <am.h>
#include <riscv/riscv.h>
#include <stdio.h>

static uint64_t boot_time = 0;
static uint64_t now = 0;
//static uint32_t now_1 = 0;
void __am_timer_init() {
  //boot_time = ind(RTC_ADDR);
  now = ind(RTC_ADDR);
}

void __am_timer_uptime(AM_TIMER_UPTIME_T *uptime) {
  //uptime->us
  now = ind(RTC_ADDR);
  uptime->us = (now) - boot_time;
  //printf("%d\n",uptime->us);
}

void __am_timer_rtc(AM_TIMER_RTC_T *rtc) {
  rtc->second = 0;
  rtc->minute = 0;
  rtc->hour   = 0;
  rtc->day    = 0;
  rtc->month  = 0;
  rtc->year   = 1900;
}

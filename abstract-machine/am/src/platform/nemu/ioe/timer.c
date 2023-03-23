#include <am.h>
#include <nemu.h>
#include <klib.h>
#include <riscv/riscv.h>

static uint64_t boot_time = 0;
static uint32_t now = 0;
static uint32_t now_1 = 0;

void __am_timer_init() {
  //boot_time = ind(RTC_ADDR);
  now = inl(RTC_ADDR);
  now_1 = inl(RTC_ADDR + 4);
}

void __am_timer_uptime(AM_TIMER_UPTIME_T *uptime) {
  //uptime->us
  now_1 = inl(RTC_ADDR + 4);
  now = inl(RTC_ADDR);
  
  uptime->us = (((uint64_t)now_1<<32) | now) - boot_time;
}

void __am_timer_rtc(AM_TIMER_RTC_T *rtc) {
  rtc->second = 0;
  rtc->minute = 0;
  rtc->hour   = 0;
  rtc->day    = 0;
  rtc->month  = 0;
  rtc->year   = 1900;
}

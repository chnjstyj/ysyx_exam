#ifndef KEYBOARD_HEAD
#define KEYBOARD_HEAD

#include <stdint.h>

void send_key(uint8_t scancode, bool is_keydown);
void init_keymap();
uint32_t key_dequeue();

#endif
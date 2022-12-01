#include <klib.h>
#include <klib-macros.h>
#include <stdint.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

size_t strlen(const char *s) {
  return (size_t)LENGTH(s);
}

char *strcpy(char *dst, const char *src) {
  size_t i = 0;
  for (i;i < strlen(src); i++)
  {
    *(dst+i) = *(src+i);
  }
  *(dst+i) = '\0';
  return dst;
}

char *strncpy(char *dst, const char *src, size_t n) {
  size_t i;
  for (i = 0; i < n && src[i] != '\0'; i++)
      dst[i] = src[i];
  for ( ; i < n; i++)
      dst[i] = '\0';

  return dst;
}

char *strcat(char *dst, const char *src) {
  size_t i;
  size_t j;
  for (i = 0; dst[i] != '\0'; i++);
  for (j = 0; src[j] != '\0'; j++)
  {
    dst[i] = src[j];
    i++;
  }
  return dst;
}

int strcmp(const char *s1, const char *s2) {
  size_t i = 0;
  for (i = 0; s1[i] != '\0' && s2[i] != '\0'; i++);
  if (s1[i] == '\0' && s2[i] == '\0') //equal
    return 0;
  else if (s1[i] == '\0')  //less
    return -1;
  else                     //greater
    return 1;
}

int strncmp(const char *s1, const char *s2, size_t n) {
  size_t i = 0;
  for (i = 0; s1[i] != '\0' && s2[i] != '\0' && i < n; i++);
  if ((s1[i] == '\0' && s2[i] == '\0') || i == n) //equal
    return 0;
  else if (s1[i] == '\0')  //less
    return -1;
  else                     //greater
    return 1;
}

void *memset(void *s, int c, size_t n) {
  size_t i;
  for (i = 0; i < n; i++)
  {
    *((char *)s+i) = c;
  }
}

void *memmove(void *dst, const void *src, size_t n) {
  size_t i = 0;
  //          source
  //  destination
  if (dst < src)
  {
    while (i < n)
    {
      *((char *)dst + i) = *((char *)src + i);
      i++;
    }
  }
  //  source
  //      destination
  else 
  {
    i = n - 1;
    while (i >= 0)
    {
      *((char *)dst + i) = *((char *)src + i);
      i--;
    }
  }
}


void *memcpy(void *out, const void *in, size_t n) {
  panic("Not implemented");
}

int memcmp(const void *s1, const void *s2, size_t n) {
  panic("Not implemented");
}

#endif

#include <fs.h>
#include <stdio.h>

typedef size_t (*ReadFn) (void *buf, size_t offset, size_t len);
typedef size_t (*WriteFn) (const void *buf, size_t offset, size_t len);

typedef struct {
  char *name;
  size_t size;
  size_t disk_offset;
  ReadFn read;
  WriteFn write;
} Finfo;

enum {FD_STDIN, FD_STDOUT, FD_STDERR, FD_FB};

size_t invalid_read(void *buf, size_t offset, size_t len) {
  panic("should not reach here");
  return 0;
}

size_t invalid_write(const void *buf, size_t offset, size_t len) {
  panic("should not reach here");
  return 0;
}

size_t serial_write(const void *buf, size_t offset, size_t len);
size_t events_read(void *buf, size_t offset, size_t len);
size_t dispinfo_read(void *buf, size_t offset, size_t len);
size_t fb_write(const void *buf, size_t offset, size_t len);
size_t sbctl_read(void *buf, size_t offset, size_t len);
size_t sbctl_write(const void *buf, size_t offset, size_t len);
size_t sb_write(const void *buf, size_t offset, size_t len);
/* This is the information about all files in disk. */
static Finfo file_table[] __attribute__((used)) = {
  [FD_STDIN]  = {"stdin", 0, 0, invalid_read, },
  [FD_STDOUT] = {"stdout", 0, 0, invalid_read, serial_write},
  [FD_STDERR] = {"stderr", 0, 0, invalid_read, serial_write},
  [3]         = {"/dev/events",0,0,events_read,invalid_write},
  [4]         = {"/dev/fb",0,0,invalid_read,fb_write},
  [5]         = {"/dev/dispinfo",0,0,dispinfo_read,invalid_write},
  [6]         = {"/dev/sb",0,0,invalid_read,sb_write},
  [7]         = {"/dev/sbctl",0,0,sbctl_read,sbctl_write},
#include "files.h"
};

uint32_t* fb = NULL;
void init_fs() {
  // TODO: initialize the size of /dev/fb
  AM_GPU_CONFIG_T t = io_read(AM_GPU_CONFIG);
  char str[50] = {0};
  dispinfo_read(str,0,50);
  fb = (uint32_t*)malloc(t.width * t.height * sizeof(uint32_t));
}

int open_offset[sizeof(file_table) / sizeof(file_table[0])] = {0};


int fs_open(const char *pathname, int flags, int mode)
{
  int i = sizeof(file_table) / sizeof(file_table[0]);
  int j;
  for (j = 0; j < i; j ++)
  {
    if (strcmp(file_table[j].name,pathname) == 0) //file name match
    {
      open_offset[j] = file_table[j].disk_offset;
      return j;
    }
  }
  Log("File not exists %s",pathname);
  assert(0);
  return -1;
}
size_t ramdisk_read(void *buf, size_t offset, size_t len);
size_t fs_read(int fd, void *buf, size_t len)
{
  int num;
  size_t r_len = len;
  if (file_table[fd].size - (open_offset[fd] - file_table[fd].disk_offset) < len && !(fd < 8)) // out of range
  {
    r_len = file_table[fd].size - (open_offset[fd] - file_table[fd].disk_offset);
    printf("Too long lens fd:%d size:%ld len:%ld rlen:%ld offset:%d %s\n",fd,file_table[fd].size,len,r_len,open_offset[fd],file_table[fd].name);
    //assert(0);
  }
  if (open_offset[fd] == file_table[fd].disk_offset + file_table[fd].size && !(fd < 6)) return 0;
  if (file_table[fd].read == NULL)
  {
    num = ramdisk_read(buf,open_offset[fd],r_len);
  }
  else 
  {
    num = file_table[fd].read(buf,open_offset[fd],r_len);
  }
  open_offset[fd] += num;
  return num;
}

size_t ramdisk_write(const void *buf, size_t offset, size_t len);
size_t fs_write(int fd, const void *buf, size_t len)
{
  int num;
  if (file_table[fd].size < len && !(fd < 8)) // out of range
  {
    Log("Too long lens fd:%d",fd);
    assert(0);
  }
  if (file_table[fd].write == NULL)
  {
    num = ramdisk_write(buf,open_offset[fd],len);
  }
  else 
  {
    num = file_table[fd].write(buf,open_offset[fd],len);
  }
  open_offset[fd] += num;
  return num;
}

size_t fs_lseek(int fd, size_t offset, int whence)
{
  switch (whence)
  {
  case SEEK_SET:
    open_offset[fd] = file_table[fd].disk_offset + offset;
    return open_offset[fd] - file_table[fd].disk_offset;
    break;
  case SEEK_CUR:
    open_offset[fd] += offset;
    return open_offset[fd] - file_table[fd].disk_offset;
    break;
  case SEEK_END:
    open_offset[fd] = file_table[fd].disk_offset + file_table[fd].size + offset;
    return open_offset[fd] - file_table[fd].disk_offset;
    break;
  default:
    Log("Error whence");
    assert(0);
    return -1;
    break;
  }
}

int fs_close(int fd)
{
  open_offset[fd] = 0;
  return 0;
}
#include "common.h"
#include <elf.h>
#ifndef FTRACE__H__
#define FTRACE__H__

typedef struct 
{
    char name[50];
    vaddr_t addr; 
}ftrace_info;

typedef struct 
{
    ftrace_info info;
    int level;
}ftrace_output;

typedef struct {
    char*      name;
    Elf64_Shdr shdr;
} SectionMap;

ftrace_info* init_ftrace(const char *elf_file,int *nums);

#endif
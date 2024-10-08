#include <elf.h>
#ifndef FTRACE__H__
#define FTRACE__H__

#define vaddr_t uint64_t

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

typedef struct 
{
    char name[50];
    vaddr_t return_addr;
}ftrace_ret;

typedef struct {
    char*      name;
    Elf64_Shdr shdr;
} SectionMap;

extern "C"  ftrace_info* init_ftrace(const char *elf_file,int *nums);
extern int ftrace_func_nums;
extern ftrace_info* ftrace_infos;
extern FILE* flog_file;
void update_ftrace(vaddr_t addr,vaddr_t return_addr);
void ret(vaddr_t addr);

#endif
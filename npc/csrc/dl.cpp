#include <dlfcn.h>
#include <assert.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include "tb_top.h"
#include "dl.h"

#define paddr_t uint64_t

void (*ref_difftest_memcpy)(paddr_t addr, void *buf, size_t n, bool direction) = NULL;
void (*ref_difftest_regcpy)(void *dut, bool direction) = NULL;
void (*ref_difftest_init)(int port) = NULL;
void (*ref_difftest_exec)(uint64_t n) = NULL;

void checkregs(uint64_t* ref_regs);

void init_difftest(char *ref_so_file)
{
    assert(ref_so_file != NULL);
    void *handle;
    handle = dlopen(ref_so_file,RTLD_LAZY);
    assert(handle);

    ref_difftest_memcpy = (void (*)(paddr_t addr, void *buf, size_t n, bool direction))dlsym(handle,"difftest_memcpy");
    assert(ref_difftest_memcpy);

    ref_difftest_regcpy = (void (*)(void *dut, bool direction))dlsym(handle,"difftest_regcpy");
    assert(ref_difftest_regcpy);    
    
    ref_difftest_init = (void (*)(int port))dlsym(handle,"difftest_init");
    assert(ref_difftest_init);  
      
    ref_difftest_exec = (void (*)(uint64_t n))dlsym(handle,"difftest_exec");
    assert(ref_difftest_exec);

    ref_difftest_init(0);
    ref_difftest_regcpy(gpr,DIFFTEST_TO_REF);
    ref_difftest_memcpy(0x80000000,pmem,1024,DIFFTEST_TO_REF);
}

void difftest_fail()
{
    exit_npc();
}

void difftest_step()
{
    uint64_t ref_regs[32] = {0};
    //由于下个周期寄存器的值才会改变
    //先对比上个周期，再执行
    ref_difftest_regcpy(ref_regs,DIFFTEST_TO_DUT);
    checkregs(ref_regs);
    //exec 
    ref_difftest_exec(1);
}

void checkregs(uint64_t* ref_regs)
{
    extern const char* regs[];
    int i;
    int j;
    bool success = true;
    for (i = 0; i < 32; i++)
    {
        if (ref_regs[i] != gpr[i]) 
        {
            printf("%s is different.\n",regs[i]);
            success = false;
        }
    }
    if (success == false)
    {
        printf("npc regs:\n");
        for (j=0; j<32; j++)
        {
            printf("%-5s:0x%016lx\n",regs[j],gpr[j]);
        }
        printf("ref regs:\n");
        for (j=0; j<32; j++)
        {
            printf("%-5s:0x%016lx\n",regs[j],ref_regs[j]);
        }
        difftest_fail();
    }
}
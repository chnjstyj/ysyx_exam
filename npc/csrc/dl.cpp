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

void checkregs(struct diff_context ref_regs);
void init_memcpy();

struct diff_context
{
   uint64_t* gpr;
   uint64_t pc;
   uint64_t* csr;
};

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
    struct diff_context c = {gpr,*pc,0};
    ref_difftest_regcpy(&c,DIFFTEST_TO_REF);
    init_memcpy();
    //ref_difftest_memcpy(0x80000000,pmem,PMEM_SIZE,DIFFTEST_TO_REF);
}

void init_memcpy()
{
    uint64_t i;
    uint8_t data;
    extern uint8_t* pmem;
    for (i = 0x80000000; i < 0x80000000 + PMEM_SIZE; i++)
    {
        //pmem_read(i,(long long *)data);
        data = *((char*)pmem + (i & 0x7fffffff));
        ref_difftest_memcpy(i,&data,1,DIFFTEST_TO_REF);
    }
}

int skip = 0;
void difftest_skip()
{
    skip = 1;
}

void difftest_fail()
{
    exit_npc();
}

void one_step();
void difftest_step()
{
    uint64_t ref_regs[32] = {0};
    uint64_t pc_ = 0;
    struct diff_context c;
    if (skip)
    {
        skip = 0;
        c = {gpr,*pc,0};
        ref_difftest_regcpy(&c,DIFFTEST_TO_REF);
    }
    c = {ref_regs,pc_,0};
    //由于下个周期寄存器的值才会改变
    //先对比上个周期，再执行
    ref_difftest_regcpy(&c,DIFFTEST_TO_DUT);
    checkregs(c);
    //exec 
    ref_difftest_exec(1);
}

void checkregs(struct diff_context ref_context)
{
    extern const char* regs[];
    int i;
    int j;
    bool success = true;
    for (i = 0; i < 32; i++)
    {
        if (ref_context.gpr[i] != gpr[i]) 
        {
            printf("%s is different.\n",regs[i]);
            success = false;
        }
    }
    /*if (ref_context.pc != *pc) 
    {
        printf("pc is different.\nnpc: %lx\nnemu: %lx\n",*pc,ref_context.pc);
        success = false;
    }*/
    if (success == false)
    {
        printf("npc regs:\n");
        for (j=0; j<32; j++)
        {
            printf("%-5s:0x%016lx\n",regs[j],gpr[j]);
        }
        printf("%-5s:0x%016lx\n","pc",*pc);
        printf("ref regs:\n");
        for (j=0; j<32; j++)
        {
            printf("%-5s:0x%016lx\n",regs[j],ref_context.gpr[j]);
        }
        printf("%-5s:0x%016lx\n","pc",ref_context.pc);
        difftest_fail();
    }
}
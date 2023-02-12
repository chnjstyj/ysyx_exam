
#ifndef __DL_H__
#define __DL_H__
#define paddr_t uint64_t

enum { DIFFTEST_TO_DUT, DIFFTEST_TO_REF };

void init_difftest(char *ref_so_file);
void difftest_step();

#endif
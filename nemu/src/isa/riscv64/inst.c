/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include "local-include/reg.h"
#include <cpu/cpu.h>
#include <cpu/ifetch.h>
#include <cpu/decode.h>

#include "ftrace.h"
#include "monitor.h"

extern ftrace_info* ftrace_infos;
extern int ftrace_func_nums;
int ftrace_level = 0;
int ftrace_ret_level = 0;
ftrace_ret ftrace_rets[30];
void update_ftrace(vaddr_t addr,vaddr_t return_addr);
void ret(vaddr_t addr);

#define R(i) gpr(i)
#define Mr vaddr_read
#define Mw vaddr_write

enum {
  TYPE_I, TYPE_U, TYPE_S,TYPE_J,TYPE_R,TYPE_B,
  TYPE_N, // none
};

#define src1R() do { *src1 = R(rs1); } while (0)
#define src2R() do { *src2 = R(rs2); } while (0)
#define immI() do { *imm = SEXT(BITS(i, 31, 20), 12); } while(0)
#define immU() do { *imm = SEXT(BITS(i, 31, 12), 20) << 12; } while(0)
#define immS() do { *imm = (SEXT(BITS(i, 31, 25), 7) << 5) | BITS(i, 11, 7); } while(0)
#define immJ() do { *imm = SEXT((BITS(i,31,31) << 20) | (BITS(i,19,12) << 12) | (BITS(i,20,20) << 11) | BITS(i,30,21) << 1,21);} while(0)
#define immB() do { *imm = SEXT((BITS(i,31,31) << 12 | BITS(i,7,7) << 11 | BITS(i,30,25) <<  5 | BITS(i,11,8) << 1),13);} while (0)

static void decode_operand(Decode *s, int *dest, word_t *src1, word_t *src2, word_t *imm, int type) {
  uint32_t i = s->isa.inst.val;
  int rd  = BITS(i, 11, 7);
  int rs1 = BITS(i, 19, 15);
  int rs2 = BITS(i, 24, 20);
  *dest = rd;
  switch (type) {
    case TYPE_I: src1R();          immI(); break;
    case TYPE_U:                   immU(); break;
    case TYPE_S: src1R(); src2R(); immS(); break;
    case TYPE_J:                   immJ(); break;
    case TYPE_R: src1R(); src2R();         break;
    case TYPE_B: src1R(); src2R(); immB(); break;
  }
}

static int decode_exec(Decode *s) {
  int dest = 0;
  word_t src1 = 0, src2 = 0, imm = 0;
  s->dnpc = s->snpc;

#define INSTPAT_INST(s) ((s)->isa.inst.val)
#define INSTPAT_MATCH(s, name, type, ... /* execute body */ ) { \
  decode_operand(s, &dest, &src1, &src2, &imm, concat(TYPE_, type)); \
  __VA_ARGS__ ; \
}

  INSTPAT_START();
  INSTPAT("??????? ????? ????? ??? ????? 00101 11", auipc  , U, R(dest) = s->pc + imm);
  INSTPAT("??????? ????? ????? ??? ????? 01101 11", lui    , U, R(dest) = imm);
  INSTPAT("??????? ????? ????? 011 ????? 00000 11", ld     , I, R(dest) = Mr(src1 + imm, 8)); //8*8 = 64 bits
  INSTPAT("???????????? ????? 000 ????? 0010011",addi,I,R(dest) = imm + src1);
  INSTPAT("???????????? ????? 000 ????? 0011011",addiw,I,R(dest) = SEXT(BITS(imm + src1,31,0),32));
  INSTPAT("???????????? ????? 010 ????? 0010011",slti,I,R(dest) = (int64_t)src1 < (int64_t)imm ? 1:0);
  INSTPAT("???????????? ????? 011 ????? 0010011",sltiu,I,R(dest) = src1 < imm ? 1:0);
  INSTPAT("???????????? ????? 111 ????? 0010011",andi,I,R(dest) = src1 & imm);
  INSTPAT("???????????? ????? 110 ????? 0010011",ori,I,R(dest) = src1 | imm);
  INSTPAT("???????????? ????? 100 ????? 0010011",xori,I,R(dest) = src1 ^ imm);
  INSTPAT("000000 ?????? ????? 001 ????? 0010011",slli,I,R(dest) = src1 << BITS(imm,5,0));
  INSTPAT("000000 ?????? ????? 101 ????? 0010011",srli,I,R(dest) = src1 >> BITS(imm,5,0));
  INSTPAT("010000 ?????? ????? 101 ????? 0010011",srai,I,R(dest) = ((int64_t)src1) >> BITS(imm,5,0));
  INSTPAT("0000000 ????? ????? 001 ????? 0011011",slliw,I,R(dest) = SEXT(BITS((uint32_t)src1 << BITS(imm,4,0),31,0),32));
  INSTPAT("0000000 ????? ????? 101 ????? 0011011",srliw,I,R(dest) = SEXT(BITS((uint32_t)src1 >> BITS(imm,4,0),31,0),32));
  INSTPAT("0100000 ????? ????? 101 ????? 0011011",sraiw,I,R(dest) = SEXT(BITS((int32_t)src1 >> BITS(imm,4,0),31,0),32));
  INSTPAT("0000000 ????? ????? 001 ????? 0110011",sll,R,R(dest) = src1 << BITS(src2,5,0));
  INSTPAT("0000000 ????? ????? 101 ????? 0110011",srl,R,R(dest) = src1 >> BITS(src2,5,0));
  INSTPAT("0100000 ????? ????? 001 ????? 0110011",sra,R,R(dest) = (long)src1 >> BITS(src2,5,0));
  INSTPAT("0000000 ????? ????? 000 ????? 0111011",addw,R,R(dest) = SEXT(BITS((uint32_t)src1 + (uint32_t)src2,31,0),32));
  INSTPAT("0100000 ????? ????? 000 ????? 0111011",subw,R,R(dest) = SEXT(BITS((uint32_t)src1 - (uint32_t)src2,31,0),32));
  INSTPAT("0000000 ????? ????? 001 ????? 0111011",sllw,R,R(dest) = SEXT(BITS((uint32_t)src1 << BITS(src2,4,0),31,0),32));
  INSTPAT("0000000 ????? ????? 101 ????? 0111011",srlw,R,R(dest) = SEXT(BITS((uint32_t)src1 >> BITS(src2,4,0),31,0),32));
  INSTPAT("0100000 ????? ????? 101 ????? 0111011",sraw,R,R(dest) = SEXT(BITS((int32_t)src1 >> BITS(src2,4,0),31,0),32));
  INSTPAT("???????????? ????? 110 ????? 0000011",lwu,I,R(dest) = (uint32_t)BITS(Mr(imm+src1,4),31,0));//零拓展，若为符号扩展，则将其先转换成同位宽的有�?�号�???
  INSTPAT("???????????? ????? 010 ????? 0000011",lw,I,R(dest) = (int32_t)BITS(Mr(imm+src1,4),31,0));
  INSTPAT("???????????? ????? 001 ????? 0000011",lh,I,R(dest) = (int16_t)BITS(Mr(imm+src1,2),15,0));
  INSTPAT("???????????? ????? 101 ????? 0000011",lhu,I,R(dest) = (uint16_t)BITS(Mr(imm+src1,2),15,0));
  INSTPAT("???????????? ????? 100 ????? 0000011",lbu,I,R(dest) = (uint8_t)BITS(Mr(imm+src1,2),7,0));
  INSTPAT("???????????? ????? 000 ????? 0000011",lb,I,R(dest) = (int8_t)BITS(Mr(imm+src1,2),7,0));//new
  //LW LH LB sign-extends LHU LBU zore-extends same to S* instructions
  INSTPAT("0100000 ????? ????? 000 ????? 0110011",sub,R,R(dest) = src1 - src2);
  INSTPAT("??????? ????? ????? 000 ????? 1100011",beq,B,s->dnpc = src1 == src2 ? imm + s->pc : s->snpc);
  INSTPAT("??????? ????? ????? 001 ????? 1100011",bne,B,s->dnpc = src1 != src2 ? imm + s->pc : s->snpc);
  INSTPAT("??????? ????? ????? 101 ????? 1100011",bge,B,s->dnpc = (int64_t)src1 >= (int64_t)src2 ? imm + s->pc : s->snpc);
  INSTPAT("??????? ????? ????? 111 ????? 1100011",bgeu,B,s->dnpc = (uint64_t)src1 >= (uint64_t)src2 ? imm + s->pc : s->snpc);//new
  INSTPAT("??????? ????? ????? 100 ????? 1100011",blt,B,s->dnpc = (int64_t)src1 < (int64_t)src2 ? imm + s->pc : s->snpc);
  INSTPAT("??????? ????? ????? 110 ????? 1100011",bltu,B,s->dnpc = (uint64_t)src1 < (uint64_t)src2 ? imm + s->pc : s->snpc);
  INSTPAT("0000000 ????? ????? 000 ????? 0110011",add,R,R(dest) = src1 + src2);
  INSTPAT("0000000 ????? ????? 111 ????? 0110011",and,R,R(dest) = src1 & src2);
  INSTPAT("0000000 ????? ????? 010 ????? 0110011",slt,R,R(dest) = (int64_t)src1 < (int64_t)src2 ? 1 : 0);
  INSTPAT("0000000 ????? ????? 011 ????? 0110011",sltu,R,R(dest) = src1 < src2 ? 1 : 0);
  INSTPAT("0000000 ????? ????? 110 ????? 0110011",or,R,R(dest) = src1 | src2);
  INSTPAT("0000000 ????? ????? 100 ????? 0110011",xor,R,R(dest) = src1 ^ src2);
  INSTPAT("0000001 ????? ????? 000 ????? 0110011",mul,R,R(dest) = (int64_t)src1 * (int64_t)src2);
  INSTPAT("0000001 ????? ????? 000 ????? 0111011",mulw,R,R(dest) = SEXT(BITS(((int32_t)src1 * (int32_t)src2),31,0),32));
  INSTPAT("0000001 ????? ????? 100 ????? 0111011",divw,R,R(dest) = SEXT(BITS(((int32_t)src1 / (int32_t)src2),31,0),32));
  INSTPAT("0000001 ????? ????? 101 ????? 0111011",divuw,R,R(dest) = SEXT(BITS(((uint32_t)src1 / (uint32_t)src2),31,0),32));
  INSTPAT("0000001 ????? ????? 110 ????? 0111011",remw,R,R(dest) = SEXT(BITS(((int32_t)src1 % (int32_t)src2),31,0),32));
  INSTPAT("0000001 ????? ????? 111 ????? 0111011",remuw,R,R(dest) = SEXT(BITS(((uint32_t)src1 % (uint32_t)src2),31,0),32));
  INSTPAT("0000001 ????? ????? 111 ????? 0110011",remu,R,R(dest) = src1 % src2);
  INSTPAT("0000001 ????? ????? 101 ????? 0110011",divu,R,R(dest) = src1 / src2);
  

  INSTPAT("? ?????????? ? ???????? ????? 1101111",jal,J,R(dest) = s->snpc;s->dnpc = s->pc + imm;update_ftrace(s->dnpc,s->snpc));
  INSTPAT("???????????? ????? 000 ????? 1100111",jalr,I,R(dest) = s->snpc;s->dnpc = src1 + imm;BITS(s->isa.inst.val, 19, 15) == 1 ? ret(s->dnpc) : update_ftrace(s->dnpc,s->snpc));
  INSTPAT("??????? ????? ????? 011 ????? 01000 11", sd     , S, Mw(src1 + imm, 8, src2));
  INSTPAT("??????? ????? ????? 010 ????? 0100011",sw,S,Mw(src1 + imm,4,src2));
  INSTPAT("??????? ????? ????? 001 ????? 0100011",sh,S,Mw(src1 + imm,2,src2));
  INSTPAT("??????? ????? ????? 000 ????? 0100011",sb,S,Mw(src1 + imm,1,src2));

  INSTPAT("0000000 00001 00000 000 00000 11100 11", ebreak , N, NEMUTRAP(s->pc, R(10))); // R(10) is $a0
  INSTPAT("??????? ????? ????? ??? ????? ????? ??", inv    , N, INV(s->pc));
  INSTPAT_END();

  R(0) = 0; // reset $zero to 0

  return 0;
}


void ret(vaddr_t addr)
{
  int i = 0;
  int k = 0;
  for (; i < ftrace_ret_level; i ++)
  {
    if (ftrace_rets[i].return_addr == addr)
    {
      ftrace_level--;
      k = ftrace_level;
      while (k != 0)
      {
        //printf(" ");
        k--;
      }
      //printf("ret  %s \n",ftrace_rets[i].name);
      break;
    }
  }
}

void update_ftrace(vaddr_t addr,vaddr_t return_addr)
{
  int i = 0;
  int k = 0;
  int j = 0;
  for (; i < ftrace_func_nums; i ++)
  {
    if (ftrace_infos[i].addr == addr)
    {
      k = ftrace_level;
      
      //strcpy(ftrace_rets[k].name,ftrace_infos[i].name);
      //ftrace_rets[k].return_addr = return_addr;
      
      while (j < ftrace_ret_level)
      {
        if (!strcmp(ftrace_rets[j].name,ftrace_infos[i].name) && ftrace_rets[j].return_addr == return_addr)
        {
          //printf("ret match\n");
          break;   //exist
        }
        j++;
      } 
      //printf("k:%d ret level: %d j:%d\n",k,ftrace_ret_level,j);
      if (j == ftrace_ret_level)  //not exist
      {
        //printf("ret dismatch\n");
        strcpy(ftrace_rets[j].name,ftrace_infos[i].name);
        ftrace_rets[j].return_addr = return_addr;
        ftrace_ret_level++;
      }
      //printf("k:%d ret level: %d \n",k,ftrace_ret_level);
      while (k != 0)
      {
        //printf(" ");
        k--;
      }
      ftrace_level ++;
      //printf("call %s(0x%lx) \n",ftrace_infos[i].name,ftrace_infos[i].addr);
    }
  }
}

int isa_exec_once(Decode *s) {
  s->isa.inst.val = inst_fetch(&s->snpc, 4);
  return decode_exec(s);
}

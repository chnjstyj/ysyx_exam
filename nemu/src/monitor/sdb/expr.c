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

#include <isa.h>
#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <memory/paddr.h>

/* We use the POSIX regex functions to process regular expressions.
 * Type 'man regex' for more information about POSIX regex functions.
 */
#include <regex.h>
#include "SeqStack.h"

#define debug

uint64_t eval(int p,int q);

enum {
  TK_NUMS,TK_HEX_NUMS,TK_REG,
  TK_NOTYPE = 256, TK_EQ,TK_NE,TK_AND,TK_DEREF

  /* TODO: Add more token types */

};

static struct rule {
  const char *regex;
  int token_type;
} rules[] = {

  /* TODO: Add more rules.
   * Pay attention to the precedence level of different rules.
   */

  {" +", TK_NOTYPE},    // spaces
  {"\\+", '+'},         // plus
  {"\\-", '-'},         // sub
  {"\\*", '*'},         // mul
  {"\\/", '/'},         // div
  {"^[0][x][0-9a-z]+",TK_HEX_NUMS}, //hex numbers
  {"[0-9]+", TK_NUMS},         // numbers
  {"^[$][0-1a-z]+",TK_REG}, //reg
  {"\\(", '('},         // left bracket
  {"\\)", ')'},         // right bracket
  {"==", TK_EQ},        // equal
  {"!=", TK_NE},        // equal
  {"&&", TK_NE},        // equal
};

#define NR_REGEX ARRLEN(rules)

static regex_t re[NR_REGEX] = {};

char* toString(uint64_t iVal)
{
  char str[1024] = {'\0',};
  char *pos = NULL;
  
  uint64_t abs = iVal;

  pos = str + 1023; //移动指针,指向堆栈底部
  *pos-- = '\0';  //end

  uint64_t dit = 0;
  while(abs > 0)
  {
    dit = abs % 10;
    abs = abs / 10;
    
    *pos-- = (char)('0' + dit); 
  }

  char *ret = (char*)malloc(1024 - (pos - str));
  
  if(iVal == 0)               
  strcpy(ret, "0");
  else                        
  strcpy(ret, pos+1);

  return(ret);    
}

uint64_t hex_str_to_int(char* str)
{
  int width = strlen(str);
  //printf("width:%d\n",width);
  int i ;
  uint64_t result = 0;
  for (i = 2; i < width; i++)
  {
    //printf("%c\n",str[i]);
    switch (str[i])
    {
      case 'a':
      case 'A':
        result += 10 * (uint64_t)pow(16,width-i-1);
        break;
      case 'b':
      case 'B':
        result += 11 * (uint64_t)pow(16,width-i-1);
        break;
      case 'c':
      case 'C':
        result += 12 * (uint64_t)pow(16,width-i-1);
        break;
      case 'd':
      case 'D':
        result += 13 * (uint64_t)pow(16,width-i-1);
        break;
      case 'e':
      case 'E':
        result += 14 * (uint64_t)pow(16,width-i-1);
        break;
      case 'f':
      case 'F':
        result += 15 * (uint64_t)pow(16,width-i-1);
        break;
      default:
        result += (str[i] - '0') * (uint64_t)pow(16,width-i-1);
    }
  }
  return result;
}

/* Rules are used for many times.
 * Therefore we compile them only once before any usage.
 */
void init_regex() {
  int i;
  char error_msg[128];
  int ret;

  for (i = 0; i < NR_REGEX; i ++) {
    ret = regcomp(&re[i], rules[i].regex, REG_EXTENDED);
    if (ret != 0) {
      regerror(ret, &re[i], error_msg, 128);
      panic("regex compilation failed: %s\n%s", error_msg, rules[i].regex);
    }
  }
}

typedef struct token {
  int type;
  char str[32];
} Token;

static Token tokens[1024] __attribute__((used)) = {};
static int nr_token __attribute__((used))  = 0;

static bool make_token(char *e) {
  int position = 0;
  int i;
  regmatch_t pmatch;

  nr_token = 0;

  #ifdef debug
  printf("%s\n",e);
  #endif

  while (e[position] != '\0') {
    /* Try all rules one by one. */
    for (i = 0; i < NR_REGEX; i ++) {
      if (regexec(&re[i], e + position, 1, &pmatch, 0) == 0 && pmatch.rm_so == 0) {
        char *substr_start = e + position;
        int substr_len = pmatch.rm_eo;

        int k;
        #ifdef debug
        Log("match rules[%d] = \"%s\" at position %d with len %d: %.*s",
            i, rules[i].regex, position, substr_len, substr_len, substr_start);
        #endif

        position += substr_len;

        /* TODO: Now a new token is recognized with rules[i]. Add codes
         * to record the token in the array `tokens'. For certain types
         * of tokens, some extra actions should be performed.
         */

        if (rules[i].token_type == TK_NUMS)
        {
          tokens[nr_token].type = TK_NUMS;
          if (substr_len > 32)
          {
            while (substr_len > 32)
            {
              for (k = 0; k < 32; k++)
              {
                tokens[nr_token].str[k] = *(substr_start + k);
              }
              substr_len -= 32;
              substr_start += 32;
              nr_token++;
            }
            for (k = 0; k < substr_len; k++)
            {
                tokens[nr_token].str[k] = *(substr_start + k);
            } 
            nr_token++;
          }
          else 
          {  
            for (k = 0; k < substr_len; k++)
            {
                tokens[nr_token].str[k] = *(substr_start + k);
            } 
            tokens[nr_token].str[substr_len] = '\0';
            nr_token++;
          }
        }
        else if (rules[i].token_type == TK_HEX_NUMS)
        {
          tokens[nr_token].type = TK_NUMS;
          uint64_t result = 0;
          char* result_str = "";
          char hex_str[32] = {'\0'};
          for (k = 0; k < substr_len; k++)
          {
              hex_str[k] = *(substr_start + k);
          } 

          result = hex_str_to_int(hex_str);          
          #ifdef debug 
          printf("hex str:%s    result:%lx\n",hex_str,result);
          #endif
          result_str = toString(result);
          for (k = 0; k < strlen(result_str); k++)
          {
              tokens[nr_token].str[k] = *(result_str + k);
          } 
          tokens[nr_token].str[strlen(result_str)] = '\0';
          #ifdef debug 
          printf("dec str:%s\n",tokens[nr_token].str);
          #endif
          nr_token++;
        }
        else if (rules[i].token_type == TK_REG)
        {
          tokens[nr_token].type = TK_REG;
          for (k = 1; k < substr_len; k++)
          {
              tokens[nr_token].str[k-1] = *(substr_start + k);
          } 
          nr_token++;
        }
        else if (rules[i].token_type != TK_NOTYPE)
        {
          tokens[nr_token].type = rules[i].token_type;
          nr_token++;
        }
        else if (rules[i].token_type == TK_NOTYPE)
        {
          ;
        }


        switch (rules[i].token_type) {
          default: //TODO();
        }

        break;
      }
    }

    if (i == NR_REGEX) {
      printf("no match at position %d\n%s\n%*.s^\n", position, e, position, "");
      return false;
    }
  }

  return true;
}


word_t expr(char *e, bool *success) {
  if (!make_token(e)) {
    *success = false;
    return 0;
  }

  /* TODO: Insert codes to evaluate the expression. */
  //TODO();
  int i;
  #ifdef debug
  for (i = 0; i < nr_token; i ++)
  {
    printf("%c     ",tokens[i].type);
    if (tokens[i].type == TK_NUMS)
      printf("%s\n",tokens[i].str);
    else 
      putchar('\n');
  }
  //printf("q:%d\n",nr_token-1);
  #endif

  for (i = 0; i < nr_token; i ++) {
  if (tokens[i].type == '*' && (i == 0 || ((tokens[i - 1].type == '(' || tokens[i - 1].type == ')' || tokens[i-1].type != TK_REG) && tokens[i - 1].type != TK_NUMS))) 
  {
    tokens[i].type = TK_DEREF;
  }
}


  uint64_t result = eval(0,nr_token-1);
  #ifdef debug
  printf("result:%ld\n",result);
  #endif
  return result;
}

int check_parentheses(int p,int q);
int find_main_op(int p,int q);

uint64_t eval(int p,int q)
{
  int i;
  int j;
  int all_nums = 1;
  for (i = p; i <= q; i++)
  {
    if(tokens[i].type != TK_NUMS)
    { 
      all_nums = 0;
      #ifdef debug
      printf("%c",tokens[i].type);
      #endif
    }
    else 
    {
      #ifdef debug
      printf("%s",tokens[i].str);
      #endif
    }
  }
  #ifdef debug
  putchar('\n');
  #endif
  if (p > q)
  {
    printf("错误的表达式\n");
    assert(0);
    return 0;
  }
  else if(p == q)
  {
    bool success;
    uint64_t reg_value;
    if (tokens[p].type == TK_NUMS)
      return (uint64_t)atol(tokens[p].str);
    else if(tokens[p].type == TK_REG)
    {
      if (!strcmp(tokens[p].str,"pc"))
      {
        return cpu.pc;
      }
      else 
      {
        reg_value = isa_reg_str2val(tokens[p].str,&success);
        if (success == true) return reg_value;
        else 
        {
          printf("错误的寄存器\n");
          assert(0);
          return 0;
        }
      }
    }
    else 
    {
      assert(0);
      return 0;
    }
  }
  else if ((p < q) && all_nums)
  {
    char str[q-p+1];
    for(i = 0;i <= q-p;i++)
    {
      for(j = 0; j < 32; j++)
      {
        str[i*32 + j] = tokens[p+i].str[i];
      }
    }
    return (uint64_t)atol(str);
  }
  else if (check_parentheses(p,q) == 1)
  {
    #ifdef debug
    printf("括号匹配\n");
    #endif
    return eval(p + 1,q - 1);
  }
  else 
  {
    int op = find_main_op(p,q);
    #ifdef debug
    printf("在此处分裂:%d\n",op);
    #endif
    uint64_t val1;
    if (op - 1 >= p)
    {
      val1 = eval(p,op-1);
    }
    else val1 = 0;
    uint64_t val2 = eval(op+1,q);
    switch (tokens[op].type)
    {
    case '+':return val1 + val2;break;
    case '-':return val1 - val2;break;
    case '*':return val1 * val2;break;
    case '/':return val1 / val2;break;
    case TK_EQ:return (val1 == val2);break;
    case TK_NE:return (val1 != val2);break;
    case TK_AND:return (val1 & val2);break;
    case TK_DEREF:
    {
      #ifdef debug 
      printf("解指针 %lx\n",val2);
      #endif 
      uint64_t mem_reuslt = paddr_read(val2,8);
      return mem_reuslt;
      break;
    }
    default:
      printf("错误的运算符 %d   %c\n",op,tokens[op].type);
      assert(0);
      return 0;
    }
  }
}

int check_parentheses(int p,int q)
{
  SeqStack s = {0};
  int i;
  int result;
  if(tokens[p].type == '(' && tokens[q].type == ')')
  {
    for (i = p; i < q; i++)
    {
      if (tokens[i].type == '(') Push(&s,'(');
      else if (tokens[i].type == ')')
      {
        if (Pop(&s,&result))
        {
          if (!isEmpty(s)) 
          {
            #ifdef debug 
            printf("括号不匹配 提前\n");
            #endif
            return 0;
          }
        }
        else 
        {
          #ifdef debug 
          printf("括号不匹配 堆栈溢出\n");
          #endif
          return 0;
        }
      }
    }
    if (Pop(&s,&result)) return 1;
    else 
    {
      #ifdef debug 
      printf("非前后括号\n");
      #endif
      return 0;
    }
  }
  else 
  {
    #ifdef debug 
    printf("无括号包围\n");
    #endif
    return 0;
  }
}

int find_main_op(int p,int q)
{
  SeqStack s = {0};
  int pop_result;
  int i;
  int level = 4;
  int op = p;
  for (i = p; i <= q; i++)
  {
    #ifdef debug
    printf("op:%c    pos:%d\n",tokens[i].type,i);
    #endif
    switch (tokens[i].type)
    {
      case TK_AND:
        if (isEmpty(s)) 
        {
          #ifdef debug 
          printf("有括号跳过&\n");
          #endif
          break;
        }
        else 
        {
          if (level > 0)
          {
            op = i;
            level = 0;
            break;
          }
          else break;
        }
      case TK_NE:
      case TK_EQ:
        if (isEmpty(s)) 
        {
          #ifdef debug 
          printf("有括号跳过== !=\n");
          #endif
          break;
        }
        else 
        {
          if (level > 1)
          {
            op = i;
            level = 2;
            break;
          }
          else break;
        }
      case '+':
      case '-':
        if (isEmpty(s)) 
        {
          #ifdef debug 
          printf("有括号跳过+-\n");
          #endif
          break;
        }
        else 
        {
          if (level > 2)
          {
            if ((i > p && tokens[i-1].type != TK_NUMS && tokens[i-1].type != '(' && tokens[i-1].type != ')' && tokens[i-1].type != TK_REG))
            {
              #ifdef debug
              printf("检测到负号，位置更新为%d   ",i-1);
              #endif
              op = i - 1;
              #ifdef debug
              printf("op = %d\n",op);
              #endif
            }
            else if ( i == p )
            {
              #ifdef debug
              printf("检测到表达式头负号,位置更新为%d\n",i);
              #endif
              op = i;
            }
            else 
            {
              op = i;
              level = 3;
            }
            break;
          }
          else break;
        }
      case '*':
      case '/':
        if (isEmpty(s))
        {
          #ifdef debug 
          printf("有括号跳过*/\n");
          #endif
          break;
        }
        else
        {
          if (level > 3)
          {
            op = i;
            break;
          }
          else break;
        }
      case '(':
        if (Push(&s,'(')) 
        {
          #ifdef debug
          printf("压栈\n");
          #endif
          break;
        }
        else 
        {
          printf("堆栈已满\n");
          assert(0);
          break;
        }
      case ')':
        if (Pop(&s,&pop_result)) 
        {
          #ifdef debug
          printf("出栈\n");
          #endif
          break;
        }
        else 
        {
          printf("堆栈为空\n");
          assert(0);
          break;
        }
      default:
        break;
    }
  }
  #ifdef debug
  printf("最终位置%d\n",op);
  #endif
  return op;
}
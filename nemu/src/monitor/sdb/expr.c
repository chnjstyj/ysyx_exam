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

/* We use the POSIX regex functions to process regular expressions.
 * Type 'man regex' for more information about POSIX regex functions.
 */
#include <regex.h>
#include "SeqStack.h"

#define debug

uint32_t eval(int p,int q);

enum {
  TK_NUMS,
  TK_NOTYPE = 256, TK_EQ,

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
  {"[0-9]+", TK_NUMS},         // numbers
  {"\\(", '('},         // left bracket
  {"\\)", ')'},         // right bracket
  {"==", TK_EQ},        // equal
};

#define NR_REGEX ARRLEN(rules)

static regex_t re[NR_REGEX] = {};

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

static Token tokens[32] __attribute__((used)) = {};
static int nr_token __attribute__((used))  = 0;

static bool make_token(char *e) {
  int position = 0;
  int i;
  regmatch_t pmatch;

  nr_token = 0;

  while (e[position] != '\0') {
    /* Try all rules one by one. */
    for (i = 0; i < NR_REGEX; i ++) {
      if (regexec(&re[i], e + position, 1, &pmatch, 0) == 0 && pmatch.rm_so == 0) {
        char *substr_start = e + position;
        int substr_len = pmatch.rm_eo;

        int k;

        Log("match rules[%d] = \"%s\" at position %d with len %d: %.*s",
            i, rules[i].regex, position, substr_len, substr_len, substr_start);

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
            nr_token++;
          }
        }
        else if (rules[i].token_type != TK_NOTYPE)
        {
          tokens[nr_token].type = rules[i].token_type;
          nr_token++;
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
  #ifdef debug
  int i;
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
  uint32_t result = eval(0,nr_token-1);
  printf("result:%d\n",result);

  return 0;
}

int check_parentheses(int p,int q);
int find_main_op(int p,int q);

uint32_t eval(int p,int q)
{
  if (p > q)
  {
    printf("错误的表达式\n");
    assert(0);
    return 0;
  }
  else if(p == q)
  {
    return atoi(tokens[p].str);
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
    printf("在此处分割:%d\n",op);
    #endif
    uint32_t val1;
    if (op - 1 >= p)
    {
      val1 = eval(p,op-1);
    }
    else val1 = 0;
    uint32_t val2 = eval(op+1,q);
    switch (tokens[op].type)
    {
    case '+':return val1 + val2;break;
    case '-':return val1 - val2;break;
    case '*':return val1 * val2;break;
    case '/':return val1 / val2;break;
    default:
      printf("错误的运算符\n");
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
  int level = 2;
  int op = p;
  for (i = p; i <= q; i++)
  {
    #ifdef debug
    printf("op:%c    pos:%d\n",tokens[i].type,i);
    #endif
    switch (tokens[i].type)
    {
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
          if ((i > p && tokens[i-1].type != TK_NUMS))
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
            level = 1;
          }
          break;
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
          if (level > 1)
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
  printf("最终位置为%d\n",op);
  #endif
  return op;
}
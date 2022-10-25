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

#define debug

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
  for (i = 0; i < 32; i ++)
  {
    printf("%c     ",tokens[i].type);
    if (tokens[i].str != NULL)
      printf("%s\n",tokens[i].str);
    else 
      putchar('\n');
  }
  #endif

  return 0;
}

bool check_parentheses(int p,int q);
int find_main_op();

int eval(int p,int q)
{
  int op;
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
  else if (check_parentheses(p,q) == true)
  {
    return eval(p + 1,q + 1);
  }
  else 
  {
    op = find_main_op();
    int val1 = eval(p,op-1);
    int val2 = eval(op+1,q);
    switch (tokens[op].type)
    {
    case '+':return val1 + val2;break;
    case '-':return val1 + val2;break;
    case '*':return val1 + val2;break;
    case '/':return val1 + val2;break;
    default:
      printf("错误的运算符\n");
      assert(0);
      return 0;
    }
  }
}

bool check_parentheses(int p,int q)
{
  
}
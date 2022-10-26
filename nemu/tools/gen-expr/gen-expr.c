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

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <assert.h>
#include <string.h>

// this should be enough
static char buf[65536] = {};
static char code_buf[65536 + 128] = {}; // a little larger than `buf`
static char *code_format =
"#include <stdio.h>\n"
"int main() { "
"  unsigned result = %s; "
"  printf(\"%%u\", result); "
"  return 0; "
"}";

char* itoa(uint32_t val, int base){
	
	static char buf[32] = {0};
	
	uint32_t i = 30;
	
	for(; val && i ; --i, val /= base)
	
		buf[i] = "0123456789abcdef"[val % base];
	
	return &buf[i+1];
	
}
	
#define max_level 5
static int buf_index = 0;
static int overflow = 0;

void gen_num();
void gen(char c);
void gen_rand_op();

uint32_t choose(uint32_t n)
{
  return rand()%n;
}

static void gen_rand_expr() {
  switch (choose(3))
  {
    case 0:gen_num();break;
    case 1:gen('('); gen_rand_expr(); gen(')'); break;
    default:gen_rand_expr(); gen_rand_op(); gen_rand_expr(); break;
  }
}

int main(int argc, char *argv[]) {
  int seed = time(0);
  srand(seed);
  int loop = 1;
  if (argc > 1) {
    sscanf(argv[1], "%d", &loop);
  }
  int i;
  for (i = 0; i < loop; i ++) {
    buf_index = 0;
    overflow = 0;
    //buf = {0};
    //level = 0;
    gen_rand_expr();
    if (overflow) 
    {
      buf[65535] = '\0';
      i = i-1;
      continue;;
    }
    buf[buf_index] = '\0';

    sprintf(code_buf, code_format, buf);

    FILE *fp = fopen("/tmp/.code.c", "w");
    assert(fp != NULL);
    fputs(code_buf, fp);
    fclose(fp);

    int ret = system("gcc /tmp/.code.c -o /tmp/.expr");
    if (ret != 0) continue;

    fp = popen("/tmp/.expr", "r");
    assert(fp != NULL);

    int result;
    if(fscanf(fp, "%d", &result))
    {
      pclose(fp);

      printf("%u %s\n", result, buf);
    }
    else pclose(fp);
  }
  return 0;
}

void gen_num()
{
  int i;
  int length;
  long width = 100;
  uint32_t result = rand()%width;
  char* str = itoa(result,10);
  length = strlen(str);
  if (length + buf_index <= 65536)
  {
    for (i = 0;i < length;i++)
    {
      buf[buf_index] = str[i];
      buf_index++;
    }
  }
  else overflow = 1;
}
void gen(char c)
{
  if (buf_index + 1 <= 65536)
  {
    buf[buf_index] = c;
    buf_index++;
  }
  else overflow = 1;
}
void gen_rand_op()
{
  if (buf_index+1 <= 65536)
  {
    int r = rand()%4;
    switch (r)
    {
    case 0:buf[buf_index] = '+';buf_index++;break;
    case 1:buf[buf_index] = '-';buf_index++;break;
    case 2:buf[buf_index] = '*';buf_index++;break;
    case 3:buf[buf_index] = '/';buf_index++;break;
    default:buf[buf_index] = '+';buf_index++;break;
    }
  }
  else overflow = 1;
}

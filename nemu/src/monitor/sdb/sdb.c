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
#include <cpu/cpu.h>
#include <readline/readline.h>
#include <readline/history.h>
#include "sdb.h"
#include <regex.h>
#include <memory/paddr.h>


static int is_batch_mode = false;
WP* wp_head = NULL;

void init_regex();
void init_wp_pool();

static inline int str_to_int(char* str)
{
  //int width = strlen(str);
  int result = atoi(str);
  return result;
}

/* We use the `readline' library to provide more flexibility to read from stdin. */
static char* rl_gets() {
  static char *line_read = NULL;

  if (line_read) {
    free(line_read);
    line_read = NULL;
  }

  line_read = readline("(nemu) ");

  if (line_read && *line_read) {
    add_history(line_read);
  }

  return line_read;
}

static int cmd_c(char *args) {
  cpu_exec(-1);
  return 0;
}


static int cmd_q(char *args) {
  nemu_state.state = NEMU_QUIT;
  return -1;
}

static int cmd_si(char *args) {
  if (args == NULL) cpu_exec(1);
  else
  {
    int steps = str_to_int(strtok(args, " "));
    cpu_exec(steps);
  } 
  return 0;
}

static int cmd_info(char *args) {
  switch (*args)
  {
  case ('r'):
    isa_reg_display();
    break; 
  case ('w'):
    if (wp_head != NULL)
    {
      WP* wp = wp_head;
      while (wp != NULL)
      {
        printf("NO:%d\tEXPR:%s\tVALUE:%ld\n",wp->NO,wp->expr,wp->result_previous);
       // if (wp->next == NULL) break;
        wp = wp->next;
      }
    }
    break;
  default:
    break;
  }
  return 0;
}

static inline int check_hex_num(char* str)
{
  int ret = 0;
  regex_t oregex;
  const char* p_regex_str = "^0[xX][0-9a-fA-F]+$";
  if ((ret = regcomp(&oregex, p_regex_str, REG_EXTENDED | REG_NOSUB)) == 0)
  {
    if ((ret = regexec(&oregex, str, 0, NULL, 0)) == 0) 
    {
      return 0;
    }
    else return 1;
  }
  else return 1;
}

static inline int str_hex_to_int(char* str,uint32_t* addr)
{
  //int width = strlen(str);
  if (check_hex_num(str) == 0)
  {
	  sscanf(str,"%x",addr);
    return 0;
  }
  else return 1;
}

static int cmd_x(char *args) 
{
  uint8_t data = 0;
  //paddr_t addr = 0;
  int nums = 0;
  char* input_expr;
  bool success;
  uint64_t result;
  int i;
  int j = 0;
  if (args == NULL)
  {
    printf("Error Input!\n");
  }
  else
  {
    nums = str_to_int(strtok(args," "));
    input_expr = strtok(NULL,"");
    printf("%d   %s\n",nums,input_expr);
    /*
    if (str_hex_to_int(input_expr,&addr) == 0)
    {
      while(j < nums)
      {
        printf("0x%x:",addr + j*4);
        for (i=3;i >= 0; i--)
        {
          data = paddr_read(addr + i + (j*4),1);
          printf("0x%02x   ",data);
        }
        putchar('\n');
        j++;
      }
    }*/
    result = expr(input_expr,&success);
    while(j < nums)
    {
      printf("0x%lx:",result + j*4);
      for (i=3;i >= 0; i--)
      {
        data = paddr_read(result + i + (j*4),1);
        printf("0x%02x   ",data);
      }
      putchar('\n');
      j++;
    }
  }
  return 0;
}

static int cmd_p(char *args) 
{
  char* input_expr;
  bool success;
  uint64_t result;
  if (args == NULL) 
  {
    printf("Error Input!\n");
  }
  else 
  {
    input_expr = args;
    result = expr(input_expr,&success);
    printf("Result of the expression is :%ld\n",result);
  }
  return 0;
}

static int cmd_w(char *args) 
{
  char* input_expr;
  bool success;
  uint64_t result;
  if (args == NULL) 
  {
    printf("Error Input!\n");
  }
  else 
  {
    input_expr = strtok(args," ");
    result = expr(input_expr,&success);
    wp_head = new_wp(input_expr,result);
  }
  return 0;
}

static int cmd_d(char *args) 
{
  int NO;
  WP* wp_d;
  if (args == NULL) printf("Please input the NO\n");
  else
  {
    NO = atoi(args);
    if (wp_head != NULL)
    {
      wp_d = wp_head;
      while (wp_d != NULL)
      {
        if (wp_d->NO == NO) 
        {
          wp_head = free_wp(wp_d);
          return 0;
        }
        else 
        {
          wp_d = wp_d->next;
        }
      }
      
    }
  }
  return 0;
}

int diff_enable = 1;

static int cmd_de(char *args) 
{
  diff_enable = 0;
  printf("detach the diff test\n");
  return 0;
}

extern void (*ref_difftest_memcpy)(paddr_t addr, void *buf, size_t n, bool direction);
extern void (*ref_difftest_regcpy)(void *dut, bool direction);
extern long img_size;

static int cmd_at(char *args) 
{
  ref_difftest_memcpy(RESET_VECTOR, guest_to_host(RESET_VECTOR), img_size, 1);
  ref_difftest_regcpy(&cpu, 1);
  diff_enable = 1;
  printf("attach the diff test\n");
  return 0;
}

static int cmd_save(char *args) 
{
  FILE* f = fopen(args,"w");
  int i;
  if (f == NULL)
  {
    printf("cannot open the file\n");
    return 1;
  }
  fprintf(f,"img_size:%ld\n",img_size);
  for (i = 0; i < 32; i++)
  {
    fprintf(f,"%lx\n",cpu.gpr[i]);
  }
  for (i = 0; i < 4; i++)
  {
    fprintf(f,"%lx\n",cpu.csr[i]);
  }
  fprintf(f,"%lx\n",cpu.pc);
  for (i = 0; i < img_size; i++)
  {
    fprintf(f,"%x\n",*(guest_to_host(RESET_VECTOR) + i));
  }
  fclose(f);
  printf("save successful\n");
  return 0;
}

static int cmd_load(char *args) 
{
  FILE* f = fopen(args,"r");
  long size;
  int result;
  int i;
  if (f == NULL)
  {
    printf("cannot open the file\n");
    return 1;
  }
  result = fscanf(f,"img_size:%ld",&size);
  for (i = 0; i < 32; i++)
  {
   result = fscanf(f,"%lx",&cpu.gpr[i]);
  }
  for (i = 0; i < 4; i++)
  {
   result = fscanf(f,"%lx",&cpu.csr[i]);
  }
  result = fscanf(f,"%lx",&cpu.pc);
  printf("%lx\n",cpu.pc);
  for (i = 0; i < size; i++)
  {
    result = fscanf(f,"%hhx",(guest_to_host(RESET_VECTOR) + i));
    //if (i == 0) printf("%hhx\n",*(guest_to_host(RESET_VECTOR) + i));
  }
  fclose(f);
  printf("load successful\n");
  return result;
}

static int cmd_help(char *args);

static struct {
  const char *name;
  const char *description;
  int (*handler) (char *);
} cmd_table [] = {
  { "help", "Display information about all supported commands", cmd_help },
  { "c", "Continue the execution of the program", cmd_c },
  { "q", "Exit NEMU", cmd_q },
  { "si", "Run one stop", cmd_si },
  { "info", "Print program status,r regfiles w watchpoints", cmd_info },
  { "x", "Print memory", cmd_x },
  { "p", "Give the result of the expression", cmd_p },
  { "w", "Set a watchpoint at the expression", cmd_w },
  { "d", "Delete a watchpoint with NO", cmd_d },
  { "detach","Detach difftest", cmd_de},
  { "attach","Attach difftest", cmd_at},
  { "save","save the snapshot", cmd_save},
  { "load","load the snapshot", cmd_load},

  /* TODO: Add more commands */

};

#define NR_CMD ARRLEN(cmd_table)

static int cmd_help(char *args) {
  /* extract the first argument */
  char *arg = strtok(NULL, " ");
  int i;

  if (arg == NULL) {
    /* no argument given */
    for (i = 0; i < NR_CMD; i ++) {
      printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
    }
  }
  else {
    for (i = 0; i < NR_CMD; i ++) {
      if (strcmp(arg, cmd_table[i].name) == 0) {
        printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
        return 0;
      }
    }
    printf("Unknown command '%s'\n", arg);
  }
  return 0;
}

void sdb_set_batch_mode() {
  is_batch_mode = true;
}

void sdb_mainloop() {
  if (is_batch_mode) {
    cmd_c(NULL);
    return;
  }

  for (char *str; (str = rl_gets()) != NULL; ) {
    char *str_end = str + strlen(str);

    /* extract the first token as the command */
    char *cmd = strtok(str, " ");
    if (cmd == NULL) { continue; }

    /* treat the remaining string as the arguments,
     * which may need further parsing
     */
    char *args = cmd + strlen(cmd) + 1;
    if (args >= str_end) {
      args = NULL;
    }

#ifdef CONFIG_DEVICE
    extern void sdl_clear_event_queue();
    sdl_clear_event_queue();
#endif

    int i;
    for (i = 0; i < NR_CMD; i ++) {
      if (strcmp(cmd, cmd_table[i].name) == 0) {
        if (cmd_table[i].handler(args) < 0) { return; }
        break;
      }
    }

    if (i == NR_CMD) { printf("Unknown command '%s'\n", cmd); }
  }
}

void init_sdb() {
  /* Compile the regular expressions. */
  init_regex();

  /* Initialize the watchpoint pool. */
  init_wp_pool();
}

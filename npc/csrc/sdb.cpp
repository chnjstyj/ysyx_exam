#include <stdio.h>
#include <stdlib.h>
#include "tb_top.h"
#include "expr.h"
#include <string.h>
#include <readline/readline.h>
#include <readline/history.h>
/*
char* readline(char* str)
{
	char input[30];
	printf("%s",str);
	scanf("%[^\n]", input);
	char* output = (char*)malloc(strlen(input));
	strcpy(output,input);
	return output;
}
*/

static inline int str_to_int(char* str)
{
  //int width = strlen(str);
  int result = atoi(str);
  return result;
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

static int cmd_c(char *args) {
  cpu_exec(-1);
  return 0;
}

static char* rl_gets() {
  static char *line_read = NULL;

  if (line_read) {
    free(line_read);
    line_read = NULL;
  }

  line_read = readline("(npc) ");
  
  if (line_read && *line_read) {
    add_history(line_read);
  }
  

  return line_read;
}

static int cmd_help(char *args);

static int cmd_info(char *args) {
  int i;
  switch (*args)
  {
  case ('r'):
    for (i=0; i<32; i++)
    {
      printf("%-5s:0x%016lx\n",regs[i],gpr[i]);
    }
    break; 
  default:
    break;
  }
  return 0;
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
    result = expr(input_expr,&success);
    printf("result:%ld\n",result);
    while(j < nums)
    {
      printf("0x%lx:",result + j*4);
      for (i=3;i >= 0; i--)
      {
        //result + i + (j*4)
        data = *(memory +result + j*4) >> 8 * i;
        printf("0x%02x   ",data);
      }
      putchar('\n');
      j++;
    }
  }
  return 0;
}

static int cmd_q(char *args) 
{
  exit_npc();
  return 0;
}

static struct {
  const char *name;
  const char *description;
  int (*handler) (char *);
} cmd_table [] = {
  { "help", "Display information about all supported commands", cmd_help },
  { "c", "Continue the execution of the program", cmd_c },
  { "si", "Run one stop", cmd_si },
  { "info", "Print program status,r regfiles w watchpoints", cmd_info },
  { "x", "Print memory", cmd_x },
  { "q", "Exit", cmd_q },

  /* TODO: Add more commands */

};

#define ARRLEN(arr) (int)(sizeof(arr) / sizeof(arr[0]))
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

void sdb_mainloop() {
  init_regex();
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

#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>
#include <stdlib.h>
#include <math.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

typedef void(*putch_ptr)(char);
char* itoa(int num,char* str,int radix);
void print_direction(int direction,const char ch,char *out,putch_ptr p);
int print_body(int direction,char *out,putch_ptr p, const char *fmt, va_list ap);
int length(char* str);

int printf(const char *fmt, ...) {
  va_list ap;
  va_start(ap,fmt);
  return print_body(0,NULL,putch,fmt,ap);
}

int vsprintf(char *out, const char *fmt, va_list ap) {
  return print_body(1,out,NULL,fmt,ap);
}

int sprintf(char *out, const char *fmt, ...) {
  va_list ap;
  va_start(ap,fmt);
  return vsprintf(out,fmt,ap);
}

int snprintf(char *out, size_t n, const char *fmt, ...) {
  panic("Not implemented");
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) {
  panic("Not implemented");
}

int print_body(int direction,char *out,putch_ptr p, const char *fmt, va_list ap)
{
  int zero_extend = 0;
  int width = 0;
  int width_i = 0;
  size_t i = 0;
  size_t j = 0;
  size_t k = 0;
  char str[32] = {0};
  char* s;
  char * num_str = NULL;
  while (*(fmt + i) != '\0')
  {
    if (*(fmt + i) == '%')
    {
      while (*(fmt + i) != 'd' && *(fmt + i) != 's' && *(fmt + i) != 'x' && *(fmt + i) != 'p')
      {
        if (*(fmt + i) == '0')
        {
          zero_extend = 1;
        }
        else if (*(fmt + i) <= '9' && *(fmt + i) >= '1')
        {
          width = *(fmt + i) - 48;
        }
        i++;
      }
      switch (*(fmt + i)) //(*(fmt + i) == 'd')
      {
        case 'd':
          i++;
          k = 0;
          num_str = itoa(va_arg(ap,int),str,10);
          if (zero_extend && length(num_str) < width)
          {
            for (width_i = 0; width_i < width - length(num_str); width_i++)
            {
              print_direction(direction,'0',out+j,p);
              j++;
            }
          }
          while (num_str[k] != '\0')
          {
            //out[j] = num_str[k];
            print_direction(direction,num_str[k],out+j,p);
            j++;k++;
          }
          break;
        case 'x':
          i++;
          k = 0;
          num_str = itoa(va_arg(ap,int),str,16);
          if (zero_extend && length(num_str) < width)
          {
            for (width_i = 0; width_i < width - length(num_str); width_i++)
            {
              print_direction(direction,'0',out+j,p);
              j++;
            }
          }
          while (num_str[k] != '\0')
          {
            //out[j] = num_str[k];
            print_direction(direction,num_str[k],out+j,p);
            j++;k++;
          }
          break;
        case 's':
          k = 0;
          s = va_arg(ap,char *);
          while (s[k] != '\0')
          {
            //out[j] = s[k];
            print_direction(direction,s[k],out+j,p);
            j++;k++;
          }
          i++;
          break;
        case 'p':
          k = 0;
          uint64_t addr = va_arg(ap,uint64_t);
          char * p_addr = (char *)malloc(8);
          p_addr = itoa(addr,p_addr,16);
          while (p_addr[k] != '\0')
          {
            //out[j] = s[k];
            print_direction(direction,p_addr[k],out+j,p);
            j++;k++;
          }
          i++;
          free(p_addr);
          break;
        default:
          break;
      }
    }
    //*(out + j) = *(fmt + i);
    print_direction(direction,*(fmt+i),out+j,p);
    j++;i++;
  }
  //*(out + j) = '\0';''
  print_direction(direction,'\0',out+j,p);
  va_end(ap);
  return j;
}

void print_direction(int direction,const char ch,char *out,putch_ptr p)
{
    if (direction)
    {
      *out = ch;
    }
    else
    {
      p(ch);
    }
}

char* itoa(int num,char* str,int radix)
{
    char index[]="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";//索引表
    unsigned unum;//存放要转换的整数的绝对值,转换的整数可能是负数
    int i=0,j,k;//i用来指示设置字符串相应位，转换之后i其实就是字符串的长度；转换后顺序是逆序的，有正负的情况，k用来指示调整顺序的开始位置;j用来指示调整顺序时的交换。

    //获取要转换的整数的绝对值
    if(radix==10&&num<0)//要转换成十进制数并且是负数
    {
        unum=(unsigned)-num;//将num的绝对值赋给unum
        str[i++]='-';//在字符串最前面设置为'-'号，并且索引加1
    }
    else unum=(unsigned)num;//若是num为正，直接赋值给unum

    //转换部分，注意转换后是逆序的
    do
    {
        str[i++]=index[unum%(unsigned)radix];//取unum的最后一位，并设置为str对应位，指示索引加1
        unum/=radix;//unum去掉最后一位

    }while(unum);//直至unum为0退出循环

    str[i]='\0';//在字符串最后添加'\0'字符，c语言字符串以'\0'结束。

    //将顺序调整过来
    if(str[0]=='-') k=1;//如果是负数，符号不用调整，从符号后面开始调整
    else k=0;//不是负数，全部都要调整

    char temp;//临时变量，交换两个值时用到
    for(j=k;j<=(i-1)/2;j++)//头尾一一对称交换，i其实就是字符串的长度，索引最大值比长度少1
    {
        temp=str[j];//头部赋值给临时变量
        str[j]=str[i-1+k-j];//尾部赋值给头部
        str[i-1+k-j]=temp;//将临时变量的值(其实就是之前的头部值)赋给尾部
    }

    return str;//返回转换后的字符串

}

int length(char* str)
{
    int i = 0;
    int j = 0;
    while (str[j] != '\0')
    {
        i++;
        j++;
    }
    return i++;
}


#endif

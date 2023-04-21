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
    char index[]="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";//������
    unsigned unum;//���Ҫת���������ľ���ֵ,ת�������������Ǹ���
    int i=0,j,k;//i����ָʾ�����ַ�����Ӧλ��ת��֮��i��ʵ�����ַ����ĳ��ȣ�ת����˳��������ģ��������������k����ָʾ����˳��Ŀ�ʼλ��;j����ָʾ����˳��ʱ�Ľ�����

    //��ȡҪת���������ľ���ֵ
    if(radix==10&&num<0)//Ҫת����ʮ�����������Ǹ���
    {
        unum=(unsigned)-num;//��num�ľ���ֵ����unum
        str[i++]='-';//���ַ�����ǰ������Ϊ'-'�ţ�����������1
    }
    else unum=(unsigned)num;//����numΪ����ֱ�Ӹ�ֵ��unum

    //ת�����֣�ע��ת�����������
    do
    {
        str[i++]=index[unum%(unsigned)radix];//ȡunum�����һλ��������Ϊstr��Ӧλ��ָʾ������1
        unum/=radix;//unumȥ�����һλ

    }while(unum);//ֱ��unumΪ0�˳�ѭ��

    str[i]='\0';//���ַ���������'\0'�ַ���c�����ַ�����'\0'������

    //��˳���������
    if(str[0]=='-') k=1;//����Ǹ��������Ų��õ������ӷ��ź��濪ʼ����
    else k=0;//���Ǹ�����ȫ����Ҫ����

    char temp;//��ʱ��������������ֵʱ�õ�
    for(j=k;j<=(i-1)/2;j++)//ͷβһһ�Գƽ�����i��ʵ�����ַ����ĳ��ȣ��������ֵ�ȳ�����1
    {
        temp=str[j];//ͷ����ֵ����ʱ����
        str[j]=str[i-1+k-j];//β����ֵ��ͷ��
        str[i-1+k-j]=temp;//����ʱ������ֵ(��ʵ����֮ǰ��ͷ��ֵ)����β��
    }

    return str;//����ת������ַ���

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

#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

char* itoa(int num,char* str,int radix);

int printf(const char *fmt, ...) {
  panic("Not implemented");
}

int vsprintf(char *out, const char *fmt, va_list ap) {
  panic("Not implemented");
}

int sprintf(char *out, const char *fmt, ...) {
  va_list ap;
  va_start(ap,fmt);
  size_t i = 0;
  size_t j = 0;
  size_t k = 0;
  char str[32] = {0};
  char* s;
  while (*(fmt + i) != '\0')
  {
    if (*(fmt + i) == '%' && *(fmt + i + 1) == 'd')
    {
      k = 0;
      char * num_str = itoa(va_arg(ap,int),str,10);
      while (num_str[k] != '\0')
      {
      	out[j] = num_str[k];
      	j++;k++;
      }
      i += 2;
    }
    else if (*(fmt + i) == '%' && *(fmt + i + 1) == 's')
    {
      k = 0;
      s = va_arg(ap,char *);
      while (s[k] != '\0')
      {
      	out[j] = s[k];
      	j++;k++;
      }
      i += 2;
    }
    else 
    {
      *(out + j) = *(fmt + i);
      j++;i++;
    }
  }
  *(out + j) = '\0';
  va_end(ap);
  return j;
}

int snprintf(char *out, size_t n, const char *fmt, ...) {
  panic("Not implemented");
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) {
  panic("Not implemented");
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

#endif

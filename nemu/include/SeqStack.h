#define MaxSize 32

typedef struct
{
    int Data[MaxSize];   // �洢Ԫ�ص�����
    int topIdx;       //ջ��ָ��
}SeqStack;


int Push(SeqStack *L,int e);

int Pop(SeqStack *L,int* result);

int isEmpty(SeqStack s);

int isFull(SeqStack s);
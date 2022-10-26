#define MaxSize 32

typedef struct
{
    int Data[MaxSize];   // 存储元素的数组
    int topIdx;       //栈顶指针
}SeqStack;


int Push(SeqStack *L,int e);

int Pop(SeqStack *L,int* result);

int isEmpty(SeqStack s);

int isFull(SeqStack s);
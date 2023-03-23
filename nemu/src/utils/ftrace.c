#include "ftrace.h"
#include <stdio.h>

int is_elf_64(FILE* fp)
{
    char buf[16];
    int  nread = fread(buf, 1, 16, fp);
    fseek(fp, 0, SEEK_SET);
    if (nread < 16) {
        return 1;
    }

    if (strncmp(buf, ELFMAG, SELFMAG)) {
        return 1;
    }

    if (buf[EI_CLASS] != ELFCLASS64) {
        return 1;
    }
    return 0;
}

ftrace_info* init_ftrace(const char *elf_file,int *nums)
{
    int r;
    FILE* felf = fopen(elf_file,"r");
    if (!felf)
    {
        printf("Error Elf File\n");
        assert(0);
    }
    if (is_elf_64(felf)) {
        printf("file type mismatch.\n");
        assert(0);
    }
    // 1 读取elf头文件
    Elf64_Ehdr m_elf;
    r = fread(&m_elf, 1, sizeof(m_elf), felf);

    // 2 读取所有段结构
    Elf64_Shdr arSection[m_elf.e_shnum];
    r = fseek(felf, m_elf.e_shoff, SEEK_SET);
    r = fread(&arSection[0], 1, (m_elf.e_shnum * m_elf.e_shentsize), felf);

    // 3 读取段名字索引
    char arSectionNames[arSection[m_elf.e_shstrndx].sh_size];
    r = fseek(felf, arSection[m_elf.e_shstrndx].sh_offset, SEEK_SET);
    r = fread(&arSectionNames, 1, sizeof(arSectionNames), felf);

    // 4 读取段结构和段名字
    SectionMap m_mpSections[m_elf.e_shnum];
    for (Elf64_Half i = 0; i < m_elf.e_shnum; i++) {
        m_mpSections[i].name = &arSectionNames[0] + arSection[i].sh_name;
        m_mpSections[i].shdr = arSection[i];
    }
    // 找到符号段
    //int strtab_size = 0;
    int symtab_size = 0;
    int size_read = 0;
    int strtab_offset = 0;
    int symtab_offset = 0;
    Elf64_Sym m_sym;
    for (Elf64_Half i = 0; i < m_elf.e_shnum; i++) {
        if (strcmp( m_mpSections[i].name,".strtab") == 0)
        {
            strtab_offset = m_mpSections[i].shdr.sh_offset;
            //strtab_size = m_mpSections[i].shdr.sh_size;
        }
        else if (strcmp( m_mpSections[i].name,".symtab") == 0)
        {
            symtab_offset = m_mpSections[i].shdr.sh_offset;
            symtab_size = m_mpSections[i].shdr.sh_size;
        }
    }
    r = fseek(felf,symtab_offset,SEEK_SET);
    //printf("strtab:%x symtab:%x\n",strtab_offset,symtab_offset);
    //printf("strtab size:%d symtab size:%d\n",strtab_size,symtab_size);
    int offsets[symtab_size/sizeof(m_sym)];
    Elf64_Addr addr[symtab_size/sizeof(m_sym)];
    int j = 0;
    while (size_read != symtab_size)
    {
        size_read += fread(&m_sym,1,sizeof(m_sym),felf);
        //fseek(felf,sizeof(m_sym),SEEK_CUR); 
        if (ELF64_ST_TYPE(m_sym.st_info) == STT_FUNC)
        {
            //printf("offset:%d\n",m_sym.st_name);
            addr[j] = m_sym.st_value;
            offsets[j] = m_sym.st_name;
            j++;
        }
    }
    //printf("j: %d\n",j);
    ftrace_info* ftrace_infos = (ftrace_info*)malloc(j);
    *nums = j;
    int k = 0;    
    while (j != 0)
    {
        fseek(felf,strtab_offset+offsets[k],SEEK_SET);
        r = fscanf(felf,"%50s",ftrace_infos[k].name);
        //printf("%s\n",ftrace_infos[k].name);
        j--;
        k++;
    }
    k = 0;
    while (k != *nums)
    {
        ftrace_infos[k].addr = addr[k];
        k++;
    }
    r++;
    return ftrace_infos;
      
}

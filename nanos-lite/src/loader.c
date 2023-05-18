#include <proc.h>
#include <elf.h>

#ifdef __LP64__
# define Elf_Ehdr Elf64_Ehdr
# define Elf_Phdr Elf64_Phdr
#else
# define Elf_Ehdr Elf32_Ehdr
# define Elf_Phdr Elf32_Phdr
#endif

size_t ramdisk_write(const void *buf, size_t offset, size_t len);
size_t ramdisk_read(void *buf, size_t offset, size_t len);
size_t get_ramdisk_size();

static uintptr_t loader(PCB *pcb, const char *filename) {
  //TODO();
  Elf_Ehdr m_elf;
  Elf_Phdr* m_elf_phs = NULL;
  int i;
  //uintptr_t start_p = 0;
  uintptr_t p = 0x0;
  ramdisk_read(&m_elf,0,sizeof(m_elf));
  //check magic number
  assert(*(uint32_t *)m_elf.e_ident == 0x464C457F);
  m_elf_phs = (Elf_Phdr*)malloc(sizeof(Elf_Phdr) * m_elf.e_phnum);
  for (i = 0; i < m_elf.e_phnum; i++)
  {
    ramdisk_read(m_elf_phs + i,m_elf.e_phoff + sizeof(Elf_Phdr) * i,sizeof(Elf_Phdr));
  }

  for (i = 0; i < m_elf.e_phnum; i++)
  {
    if ((m_elf_phs + i)->p_type == PT_LOAD)
    {
      //start_p = (m_elf_phs + i)->p_vaddr;
    }
  }
  for (i = 0; i < m_elf.e_phnum; i++)
  {
    //LOAD
    if ((m_elf_phs + i)->p_type == PT_LOAD)
    {
      //printf("load %x %x %x\n",p,(m_elf_phs + i)->p_vaddr,(uint64_t)p + (m_elf_phs + i)->p_vaddr);
      ramdisk_read((void *)((uint64_t)p + (m_elf_phs + i)->p_vaddr),(m_elf_phs + i)->p_offset,(m_elf_phs + i)->p_filesz);
      
      memset((void *)((uint64_t)p + (m_elf_phs + i)->p_vaddr + (m_elf_phs + i)->p_filesz),0,(m_elf_phs + i)->p_memsz - (m_elf_phs + i)->p_filesz);
    }
  }
  //printf("test %x\n",sizeof(Elf_Ehdr));
  p = m_elf.e_entry;
  return p;
}

void naive_uload(PCB *pcb, const char *filename) {
  uintptr_t entry = loader(pcb, filename);
  Log("Jump to entry = %p", entry);
  ((void(*)())entry) ();
}


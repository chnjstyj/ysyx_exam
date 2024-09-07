ENTRY(_start)

SECTIONS {
  /* _pmem_start and _entry_offset are defined in LDFLAGS */
  . = _pmem_start + _entry_offset;
  .text : {
    *(entry)
    *(.text*)
  }
  etext = .;
  _etext = .;
  .rodata : {
    *(.rodata*)
  }
  .data : {
    *(.data)
  }
  edata = .;
  _data = .;
  .bss : {
	_bss_start = .;
    *(.bss*)
    *(.sbss*)
    *(.scommon)
  }
  . = 0xf000000;
  _stack_top = ALIGN(0xf000000);
  . = _stack_top + 0x1fff;
  _stack_pointer = .;
  end = .;
  _end = .;
  _heap_start = ALIGN(0xf000000);
}

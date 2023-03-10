include $(AM_HOME)/scripts/isa/riscv64.mk

BIN_FILE = build/$(ALL)-$(ARCH).bin
HEX_FILE = build/$(ALL)-$(ARCH).rom
ELF_FILE = build/$(ALL)-$(ARCH).elf
INST_FILE = build/$(ALL)-$(ARCH).txt

AM_SRCS := riscv/npc/start.S \
           riscv/npc/trm.c \
           riscv/npc/ioe.c \
           riscv/npc/timer.c \
           riscv/npc/input.c \
           riscv/npc/cte.c \
           riscv/npc/trap.S \
           platform/dummy/vme.c \
           platform/dummy/mpe.c

CFLAGS    += -fdata-sections -ffunction-sections
LDFLAGS   += -T $(AM_HOME)/scripts/linker.ld --defsym=_pmem_start=0x80000000 --defsym=_entry_offset=0x0
LDFLAGS   += --gc-sections -e _start
CFLAGS += -DMAINARGS=\"$(mainargs)\"
.PHONY: $(AM_HOME)/am/src/riscv/npc/trm.c

image: $(IMAGE).elf
	@$(OBJDUMP) -d $(IMAGE).elf > $(IMAGE).txt
	@echo + OBJCOPY "->" $(IMAGE_REL).bin
	@$(OBJCOPY) -S --set-section-flags .bss=alloc,contents -O binary $(IMAGE).elf $(IMAGE).bin
	

run: image
	hexdump -v -e '1/4 "%08x\n"' $(BIN_FILE) > $(HEX_FILE)
	cp $(HEX_FILE) $(NPC_HOME)/inst.rom
	cp $(BIN_FILE) $(NPC_HOME)/inst.bin
	cp $(ELF_FILE) $(NPC_HOME)/inst_rom.elf
	cp $(INST_FILE) $(NPC_HOME)/inst_rom.txt
	cd $(NPC_HOME) && ./Vtop diff elf


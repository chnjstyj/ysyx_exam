include $(AM_HOME)/scripts/isa/riscv64.mk

BIN_FILE = build/$(NAME)-$(ARCH).bin
HEX_FILE = build/$(NAME)-$(ARCH).rom
ELF_FILE = build/$(NAME)-$(ARCH).elf
INST_FILE = build/$(NAME)-$(ARCH).txt

AM_SRCS := riscv/ysyxsoc/start.S \
           riscv/ysyxsoc/trm.c \
           riscv/ysyxsoc/ioe.c \
           riscv/ysyxsoc/timer.c \
           riscv/ysyxsoc/input.c \
           riscv/ysyxsoc/gpu.c \
           riscv/ysyxsoc/audio.c \
           riscv/ysyxsoc/cte.c \
           riscv/ysyxsoc/trap.S \
           platform/dummy/vme.c \
           platform/dummy/mpe.c

CFLAGS    += -fdata-sections -ffunction-sections
LDFLAGS   += -T $(AM_HOME)/am/src/riscv/ysyxsoc/linker.ld --defsym=_pmem_start=0x20000000 --defsym=_entry_offset=0x0
LDFLAGS   += --gc-sections -e _start
CFLAGS += -DMAINARGS=\"$(mainargs)\"
.PHONY: $(AM_HOME)/am/src/riscv/ysyxsoc/trm.c

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
	cd $(NPC_HOME) && make sim && ./Vtop


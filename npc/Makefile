BUILD_DIR = ./build ./obj_dir
VSRCS = $(shell find $(abspath ./build) -name "*.v")
CSRCS = $(shell find $(abspath ./csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")
top_name ?= top

export PATH := $(PATH):$(abspath ./utils)

VERILATOR_CFLAGS += -MMD --build -cc  \
				-O3 --x-assign fast --x-initial fast --noassert

test:
	mill -i __.test

verilog:
	$(call git_commit, "generate verilog")
	mkdir -p $(BUILD_DIR)
	mill -i __.test.runMain Elaborate -td $(BUILD_DIR)

help:
	mill -i __.test.runMain Elaborate --help

compile:
	mill -i __.compile

bsp:
	mill -i mill.bsp.BSP/install

reformat:
	mill -i __.reformat

checkformat:
	mill -i __.checkFormat

clean:
	-rm -rf $(BUILD_DIR) auto_bind.cpp

# rules for NVBoard
include $(NVBOARD_HOME)/scripts/nvboard.mk

# rules for verilator
INCFLAGS = $(addprefix -I, $(INC_PATH))
CFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\""
LDFLAGS += -lSDL2 -lSDL2_image

verilator:${VSRCS} $(CSRCS) $(NVBOARD_ARCHIVE) auto_bind.cpp
	verilator $(VERILATOR_CFLAGS) \
	--top-module $(top_name) $^ \
	$(addprefix -CFLAGS , $(CFLAGS)) $(addprefix -LDFLAGS , $(LDFLAGS)) \
	--exe -o ../Vtop

auto_bind.cpp: constr/${top_name}.nxdc
	python3 $(NVBOARD_HOME)/scripts/auto_pin_bind.py $^ $@

.PHONY: test verilog help compile bsp reformat checkformat clean

sim:verilog
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "Write this Makefile by yourself."
	$(MAKE) verilator

include ../Makefile

BUILD_DIR = ./build ./obj_dir
VSRCS = $(shell find $(abspath ./build) -name "*.v")
CSRCS = $(shell find $(abspath ./csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")
top_name ?= ysyxSoCFull
DOCKER_RUN = sudo docker run --rm --log-driver=json-file\
-v $(shell pwd):/mnt/data \
--hostname lizhen --mac-address 02:42:ac:11:00:02 \
vcs_docker /usr/synopsys/vc_static-O-2018.09-SP2-2/verdi/bin/vcd2fsdb /mnt/data/waveform.vcd -o /mnt/data/waveform.fsdb
DOCKER_EXEC = sudo docker exec -it vcs 
DOCKER_CLEAN = sudo docker stop vcs && sudo docker rm vcs

export PATH := $(PATH):$(abspath ./utils)

run:
	-./Vtop elf diff

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
#include $(NVBOARD_HOME)/scripts/nvboard.mk

#VERILATOR_CFLAGS += --prof-cfuncs 
VERILATOR_CFLAGS += --savable --trace -MMD --build -cc --autoflush  \
				-O3 --x-assign fast --x-initial fast --noassert -I../ysyxSoC/perip/uart16550/rtl -I../ysyxSoC/perip/spi/rtl

# rules for verilator
#INC_PATH += $(abspath ../ysyxSoC/perip/uart16550/rtl) $(abspath ../ysyxSoC/perip/spi/rtl)
INCFLAGS = $(addprefix -I, $(INC_PATH))
CFLAGS += -g $(INCFLAGS) $(shell llvm-config --cxxflags) -fPIE -DTOP_NAME="\"V$(TOPNAME)\""
LDFLAGS += -lSDL2 -lSDL2_image -lcurses -lreadline $(shell llvm-config --libs)
VSRCS += $(shell find $(abspath ../ysyxSoC/perip) -name "*.v")
VSRCS += $(shell find $(abspath ../ysyxSoC/build) -name "*.v")

verilator:${VSRCS} $(CSRCS)
	verilator $(VERILATOR_CFLAGS) \
	--top-module $(top_name) $^ \
	--timescale "1ns/1ns" --no-timing \
	$(addprefix -CFLAGS , $(CFLAGS)) $(addprefix -LDFLAGS , $(LDFLAGS)) \
	--exe -o ../Vtop 

auto_bind.cpp: constr/${top_name}.nxdc
	python3 $(NVBOARD_HOME)/scripts/auto_pin_bind.py $^ $@

.PHONY: test verilog help compile bsp reformat checkformat clean

sim:verilog
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "Write this Makefile by yourself."
	$(MAKE) verilator
wave:
	gtkwave waveform.vcd -a waveform.gtkw

fsdb:
	$(DOCKER_RUN)


include ../Makefile

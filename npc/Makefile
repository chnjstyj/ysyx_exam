v_dir = vsrc
cpp_dir = csrc
top_name ?= top
include $(NVBOARD_HOME)/scripts/nvboard.mk

v_file = ${v_dir}/top.v
cpp_file = ${cpp_dir}/tb_top.cpp

run_without_cpp:${v_dir}/top.v
	verilator -cc vsrc/top.v

all:
	@echo "Write this Makefile by your self."
	@echo

sim:
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "Write this Makefile by your self."
	$(MAKE) run

INCFLAGS = $(addprefix -I, $(INC_PATH))
CFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(top_name)\""
LDFLAGS += -lSDL2 -lSDL2_image

run:${v_file} ${cpp_file} auto_bind.cpp $(NVBOARD_ARCHIVE)
	verilator -MMD --build -cc \
		-O3 --x-assign fast --x-initial fast --noassert \
		--top-module ${top_name} $^ \
		$(addprefix -CFLAGS , $(CFLAGS)) $(addprefix -LDFLAGS , $(LDFLAGS)) \
		--exe -o /

auto_bind.cpp: constr/${top_name}.nxdc
	python3 $(NVBOARD_HOME)/scripts/auto_pin_bind.py $^ $@

.PHONY: clean
clean:
	rm -rf obj_dir *.vcd

include ../Makefile

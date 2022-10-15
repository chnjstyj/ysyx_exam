v_dir = vsrc
cpp_dir = csrc
constr_name ?= top
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

LDFLAGS += -lSDL2 -lSDL2_image

run:${v_file} ${cpp_file} auto_bind.cpp $(NVBOARD_ARCHIVE)
	verilator -Wall -cc \
		$(addprefix -LDFLAGS , $(LDFLAGS)) \
		 ${v_dir}/top.v --exe ${cpp_dir}/tb_top.cpp $(NVBOARD_ARCHIVE)

auto_bind.cpp: constr/${constr_name}.nxdc
	python3 $(NVBOARD_HOME)/scripts/auto_pin_bind.py $^ $@

.PHONY: clean
clean:
	rm -rf obj_dir *.vcd

include ../Makefile

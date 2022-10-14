
run_without_cpp:vsrc/top.v
	verilator -cc vsrc/top.v

all:
	@echo "Write this Makefile by your self."

sim:
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "Write this Makefile by your self."

run:top.v tb_top.cpp
	verilator -Wall --trace -cc top.v --exe tb_top.cpp
	make -C obj_dir -f Vtop.mk Vtop

include ../Makefile

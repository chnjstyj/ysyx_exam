v_dir = vsrc
cpp_dir = csrc



run_without_cpp:${v_dir}/top.v
	verilator -cc vsrc/top.v

all:
	@echo "Write this Makefile by your self."

sim:${v_dir}/top.v ${cpp_dir}/tb_top.cpp
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "Write this Makefile by your self."
	verilator -Wall --trace -cc ${v_dir}/top.v --exe ${cpp_dir}/tb_top.cpp
	make -C obj_dir -f Vtop.mk Vtop
	obj_dir/Vtop

run:${v_dir}/top.v ${cpp_dir}/tb_top.cpp
	verilator -Wall --trace -cc ${v_dir}/top.v --exe ${cpp_dir}/tb_top.cpp
	make -C obj_dir -f Vtop.mk Vtop
	obj_dir/Vtop

.PHONY: clean
clean:
	rm -rf obj_dir *.vcd

include ../Makefile

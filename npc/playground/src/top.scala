import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile
import chisel3.util.experimental.loadMemoryFromFileInline

class top extends Module{
    val io = IO(new Bundle{
        val VGA_HSYNC = Output(UInt(1.W))
        val VGA_VSYNC = Output(UInt(1.W))
        val VGA_BLANK_N = Output(UInt(1.W))
        val VGA_R = Output(UInt(8.W))
        val VGA_G = Output(UInt(8.W))
        val VGA_B = Output(UInt(8.W))
    })

    val mem = Mem(307200,UInt(24.W)) 
    val addr = RegInit(0.U(64.W))
    loadMemoryFromFile(mem,"pic.hex")
    val vga_ctrl = Module(new vga_ctrl)

    val vga_data = Wire(UInt(24.W))
    val h_addr = Wire(UInt(10.W))
    val v_addr = Wire(UInt(10.W))

    when (vga_ctrl.io.valid === 1.U){
        vga_data := mem(addr)
    }.otherwise{
        vga_data := 0.U(24.W)
    }

    when(vga_ctrl.io.valid === 1.U){
        addr := addr + 1.U
        //vga_data := mem(addr)
        when(h_addr === 639.U && v_addr === 479.U){
        addr := 0.U
        }
    }.otherwise{
        //addr := 0.U
    }



    vga_ctrl.io.vga_data := vga_data
    h_addr := vga_ctrl.io.h_addr
    v_addr := vga_ctrl.io.v_addr
    io.VGA_HSYNC := vga_ctrl.io.hsync
    io.VGA_VSYNC := vga_ctrl.io.vsync
    io.VGA_BLANK_N := vga_ctrl.io.valid
    io.VGA_R := vga_ctrl.io.vga_r
    io.VGA_G := vga_ctrl.io.vga_g
    io.VGA_B := vga_ctrl.io.vga_b

}
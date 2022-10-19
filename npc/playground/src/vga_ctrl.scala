import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile
import chisel3.util.experimental.loadMemoryFromFileInline

class vga_ctrl extends Module{
    val io =IO(new Bundle{
        val vga_data = Input(UInt(24.W))
        val h_addr = Output(UInt(10.W))
        val v_addr = Output(UInt(10.W))
        val hsync = Output(UInt(1.W))
        val vsync = Output(UInt(1.W))
        val valid = Output(UInt(1.W))
        val vga_r = Output(UInt(8.W))
        val vga_g = Output(UInt(8.W))
        val vga_b = Output(UInt(8.W))
    })

    //parameters
    val h_frontporch = 96.U
    val h_active = 144.U
    val h_backporch = 784.U
    val h_total = 800.U

    val v_frontporch = 2.U
    val v_active = 35.U
    val v_backporch = 515.U
    val v_total = 525.U

    val x_cnt = RegInit(1.U(10.W))
    val y_cnt = RegInit(1.U(10.W))
    val h_valid = Wire(UInt(1.W))
    val v_valid = Wire(UInt(1.W))

    when(x_cnt === h_total - 1.U){
        x_cnt := 1.U 
        when(y_cnt === v_total - 1.U){
            y_cnt := 1.U 
        }.otherwise{
            y_cnt := y_cnt + 1.U
        }
    }.otherwise{
        x_cnt := x_cnt + 1.U
    }

    //signal
    io.hsync := (x_cnt > h_frontporch)
    io.vsync := (y_cnt > v_frontporch)

    h_valid := (x_cnt > h_active) && (x_cnt <= h_backporch)
    v_valid := (y_cnt > v_active) && (y_cnt <= v_backporch)
    io.valid := h_valid & v_valid

    io.h_addr := Mux(h_valid === 1.U,x_cnt - 145.U,0.U)
    io.v_addr := Mux(v_valid === 1.U,y_cnt - 36.U,0.U)

    io.vga_r := io.vga_data(23,16)
    io.vga_g := io.vga_data(15,8)
    io.vga_b := io.vga_data(7,0)

}
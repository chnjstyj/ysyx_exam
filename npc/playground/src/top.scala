import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile
import chisel3.util.experimental.loadMemoryFromFileInline

class top extends Module{
    val io = IO(new Bundle{
        val ps2_clk = Input(UInt(1.W))
        val ps2_data = Input(UInt(1.W))
        val VGA_HSYNC = Output(UInt(1.W))
        val VGA_VSYNC = Output(UInt(1.W))
        val VGA_BLANK_N = Output(UInt(1.W))
        val VGA_R = Output(UInt(8.W))
        val VGA_G = Output(UInt(8.W))
        val VGA_B = Output(UInt(8.W))
        val h_count = Output(UInt(8.W))
        val v_count = Output(UInt(8.W))
        val font_addr = Output(UInt(32.W))
    })

    val vga_ctrl = Module(new vga_ctrl)
    val ps2 = Module(new ps2)

    val vga_data = Wire(UInt(24.W))
    val h_addr = Wire(UInt(10.W))
    val v_addr = Wire(UInt(10.W))

    val h_char = RegInit(0.U(8.W))
    val v_char = RegInit(0.U(8.W))

    val v_count = RegInit(0.U(8.W))
    val h_count = RegInit(0.U(8.W))

    /*
    val mem = Mem(307200,UInt(24.W)) 
    val addr = RegInit(0.U(64.W))
    loadMemoryFromFile(mem,"pic.hex")*/

    val font_mem = Mem(4096,UInt(12.W))
    loadMemoryFromFile(font_mem,"vga_font.txt")
    val font_vec = Mem(2176,UInt(8.W))
    val font_addr = WireDefault(v_count * 71.U + h_count)
    io.font_addr := font_addr

    val write_ptr = RegInit(0.U(12.W))
    val writed_lines = RegInit(0.U(8.W))

    //val ps2_data_ready

    //initial
    /*
    for (i <- 0 until 2176)
    {
        font_vec(i) := "h00".U
    }*/

    when((ps2.io.valid & ps2.io.ready) === 1.U){
        //enter
        when(ps2.io.data === "h0a".U ){
            //end of line
            when(write_ptr - 71.U * writed_lines === 0.U){
                //write_ptr := write_ptr + 1.U
                //writed_lines := writed_lines + 1.U
            }.otherwise{
                write_ptr := write_ptr + (71.U - (write_ptr - (writed_lines - 1.U) * 71.U))
                font_vec(write_ptr) := 0.U
                writed_lines := writed_lines + 1.U
            }
        //backspace
        }.elsewhen(ps2.io.data === "h08".U){
            font_vec(write_ptr) := "h20".U
            when (write_ptr =/= 0.U)
            {
                write_ptr := write_ptr - 1.U
            }
        } .otherwise{
            when(ps2.io.data =/= 0.U){
                write_ptr := write_ptr + 1.U
                font_vec(write_ptr) := ps2.io.data
                when(write_ptr - 71.U * writed_lines === 0.U){
                    writed_lines := writed_lines + 1.U
                }
            }
        }
    }.otherwise{
        font_vec(write_ptr) := 0.U
    }

    when(vga_ctrl.io.valid === 1.U){
        h_char := h_char + 1.U 
        when(h_char - 8.U === 0.U){
            when (h_count === 70.U){
                h_count := 0.U
            }.otherwise{
                h_count := h_count + 1.U
                h_char := 0.U
            }
        }
        when(h_addr === 639.U){
            h_char := 0.U 
            h_count := 0.U 
            v_char := v_char + 1.U
        }
        //when(v_valid === 1.U){
            when(v_char - 16.U === 0.U){
                v_count := v_count + 1.U 
                v_char := 0.U 
            }
            when(v_addr === 0.U){
                v_count := 0.U
            }
        //}
    }.otherwise{
        h_char := 0.U 
        h_count := 0.U
    }

    when (vga_ctrl.io.valid === 1.U){
        when(font_addr < 4096.U){
            when((font_mem((font_vec(font_addr) << 4) + v_char))(h_char) === 1.U){
                vga_data := "b11111111_11111111_11111111".U 
            }.otherwise{
                vga_data := 0.U
            }
        }.otherwise{
            vga_data := 0.U
        }
        //vga_data := mem(addr)
    }.otherwise{
        vga_data := 0.U(24.W)
    }

    /*
    when(vga_ctrl.io.valid === 1.U){
        addr := addr + 1.U
        when(h_addr === 639.U && v_addr === 479.U){
        addr := 0.U
        }
    }.otherwise{
        //addr := 0.U
    }*/

    vga_ctrl.io.vga_data := vga_data
    h_addr := vga_ctrl.io.h_addr
    v_addr := vga_ctrl.io.v_addr
    io.VGA_HSYNC := vga_ctrl.io.hsync
    io.VGA_VSYNC := vga_ctrl.io.vsync
    io.VGA_BLANK_N := vga_ctrl.io.valid
    io.VGA_R := vga_ctrl.io.vga_r
    io.VGA_G := vga_ctrl.io.vga_g
    io.VGA_B := vga_ctrl.io.vga_b

    ps2.io.ps2_clk := io.ps2_clk
    ps2.io.ps2_data := io.ps2_data
    
    io.h_count := h_count
    io.v_count := v_count
    /*
    h_char := vga_ctrl.io.h_char
    v_char := vga_ctrl.io.v_char*/

}
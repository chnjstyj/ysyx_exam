import chisel3._
import chisel3.util._

class div(
    xlen:Int 
) extends Module{
    val io = IO(new Bundle{
        val dividend = Input(UInt(xlen.W))
        val divisor = Input(UInt(xlen.W))
        val div_valid = Input(Bool())
        val divw = Input(Bool())
        val div_signed = Input(Bool())
        val flush = Input(Bool())
        val div_ready = Output(Bool())
        val out_valid = Output(Bool())
        val quotient = Output(UInt(xlen.W))
        val remainder = Output(UInt(xlen.W))
    })

    val dividend_s = RegInit(0.U((2 * xlen).W))
    val divisor_e = RegInit(0.U((xlen + 1).W)) 
    val counter = RegInit(0.U(log2Ceil(xlen).W))
    val counter_max = RegInit((xlen - 1).U)

    val quotient = RegInit(0.U(xlen.W))
    val remainder = RegInit(0.U(xlen.W))
    val quotient_sign = RegInit(true.B)
    val remainder_sign = RegInit(true.B)
    val valid = RegInit(false.B)
    val ready = RegInit(false.B)
    val running = RegInit(false.B)
    val divw = RegInit(false.B)

    val mask = WireDefault(UInt((2*xlen).W),((1.U << (xlen-1).U) - 1.U))
    val part = WireDefault(dividend_s(2*xlen-1,xlen-1))
    val subs = WireDefault((part - divisor_e))
    //val substi = WireDefault(subs << ((1 << xlen) - 1))

    io.div_ready := ready
    io.out_valid := valid 
    io.remainder := remainder
    io.quotient := quotient

    /*
    when (divw){
        mask := ((1.U << (xlen/2-1).U) - 1.U)
        part := dividend_s(xlen-1,xlen/2-1)
        subs := (part - divisor_e)
        substi := subs << ((1 << (xlen/2-1)) - 1).U
    }.otherwise{
        mask := ((1.U << (xlen-1).U) - 1.U)
        part := dividend_s(2*xlen-1,xlen-1)
        subs := (part - divisor_e)
        substi := subs << ((1 << (xlen-1)) - 1).U
    }*/

    when (io.div_valid){
        when (io.dividend(xlen-1) === 1.U && io.div_signed){
            remainder_sign := false.B 
        }.elsewhen (io.divw && io.div_signed && io.dividend(xlen/2-1) === 1.U){
            remainder_sign := false.B 
        }.otherwise{
            remainder_sign := true.B 
        }
    }    
    when (io.div_valid){
        when ((io.dividend(xlen-1) ^ io.divisor(xlen-1) === 1.U) && io.div_signed){
            quotient_sign := false.B 
        }.elsewhen (io.divw && io.div_signed && (io.dividend(xlen/2-1) ^ io.divisor(xlen/2-1) === 1.U)){
            quotient_sign := false.B
        }.otherwise{
            quotient_sign := true.B 
        }
    }

    when (io.flush){
        counter := 0.U 
        dividend_s := 0.U 
        divisor_e := 0.U
        ready := true.B 
        valid := false.B
        quotient := 0.U 
        remainder := 0.U
    }.elsewhen (io.div_valid){
        ready := false.B
        valid := false.B 
        divw := io.divw
        //when (io.divw){
            //counter_max := (xlen/2 - 1).U 
        //}.otherwise{
            counter_max := (xlen-1).U
        //}
        when (io.dividend(xlen-1) =/= 1.U && !io.divw){
            dividend_s := Cat(0.U(xlen.W),io.dividend)
        }.elsewhen (io.dividend(xlen/2-1) === 1.U && io.divw && io.div_signed){
            dividend_s := Cat(0.U((xlen + xlen/2).W),(~(io.dividend(xlen/2-1,0)) + 1.U))
        }.elsewhen (io.divw){
            dividend_s := Cat(0.U((xlen + xlen/2).W),io.dividend(xlen/2-1,0))
        }.otherwise{
            dividend_s := Cat(0.U(xlen.W),(~io.dividend + 1.U))
        }
        when (io.divisor(xlen-1) =/= 1.U && !io.divw){
            divisor_e := Cat(0.U(1.W),io.divisor)
        }.elsewhen (io.divisor(xlen/2-1) === 1.U && io.divw && io.div_signed){
            divisor_e := Cat(0.U(1.W),(~(io.divisor(xlen/2-1,0)) + 1.U))
        }.elsewhen (io.divw){
            divisor_e := Cat(0.U(1.W),io.divisor(xlen/2-1,0))
        }.otherwise{
            divisor_e := Cat(0.U(1.W),(~io.divisor + 1.U))
        }
        running := true.B 
        counter := 0.U
        quotient := 0.U 
        remainder := 0.U
    }.elsewhen (running && counter =/= counter_max){
        counter := counter + 1.U
        when ((subs).asSInt >= 0.S){
            quotient := quotient | (1.U << (counter_max - counter))
            dividend_s := ((dividend_s & mask) | (subs << (xlen-1).U)) << 1.U
        }.otherwise{
            dividend_s := dividend_s << 1.U
        }
    }.elsewhen (running && counter === counter_max){
        when ((subs).asSInt >= 0.S){
            quotient := quotient | (1.U << (counter_max - counter))
            dividend_s := ((dividend_s & mask) | (subs << (xlen-1).U)) << 1.U
        }.otherwise{
            dividend_s := dividend_s << 1.U
        }
        when (!quotient_sign){
            quotient := ~quotient + 1.U 
        }
        //when (!remainder_sign){
            //remainder := ~subs + 1.U 
        //}.otherwise{
        when (subs.asSInt < 0.S){
            remainder := Mux(remainder_sign,dividend_s(2*xlen-2,xlen-1),~dividend_s(2*xlen-2,xlen-1) + 1.U)
        }.otherwise{
            remainder := Mux(remainder_sign,subs,~subs+1.U)
        }
        running := false.B
        valid := true.B 
        ready := true.B  
        counter := 0.U 
    }.otherwise{
        valid := false.B 
    }
}
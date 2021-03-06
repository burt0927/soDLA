package nvdla

import chisel3._
import chisel3.experimental._


class NV_NVDLA_HLS_shiftleftsu extends Module {
    
    val IN_WIDTH = 16
    val OUT_WIDTH = 32
    val SHIFT_WIDTH = 6
    val SHIFT_MAX = Math.pow(2, SHIFT_WIDTH).toInt - 1
    val HIGH_WIDTH = SHIFT_MAX + IN_WIDTH - OUT_WIDTH
    
    val io = IO(new Bundle {      
        val data_in = Input(UInt(IN_WIDTH.W))
        val shift_num = Input(UInt(SHIFT_WIDTH.W))
        val data_out = Output(UInt(OUT_WIDTH.W))
    })
        
    val data_high = Wire(UInt(HIGH_WIDTH.W))
    val data_shift = Wire(UInt(OUT_WIDTH.W))
    val data_max = Wire(UInt(OUT_WIDTH.W))
    val data_sign = Wire(Bool())
    val left_shift_sat = Wire(Bool())
    
    data_sign := io.data_in(IN_WIDTH-1)
    
    data_high := (Cat(Fill(SHIFT_MAX, data_sign), io.data_in) << io.shift_num)(HIGH_WIDTH + OUT_WIDTH - 1, OUT_WIDTH)
    
    data_shift := (Cat(Fill(SHIFT_MAX, data_sign), io.data_in) << io.shift_num)(OUT_WIDTH - 1, 0)
    
    left_shift_sat := Cat(data_high, data_shift) != Fill(HIGH_WIDTH+1, data_sign)
    
    data_max := Mux(data_sign, Cat(true.B, Fill(OUT_WIDTH-1, false.B)), ~Cat(true.B, Fill(OUT_WIDTH-1, false.B)))
    
    io.data_out := Mux(left_shift_sat, data_max, data_shift)
    

       
}


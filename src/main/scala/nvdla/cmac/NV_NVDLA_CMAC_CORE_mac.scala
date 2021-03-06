package nvdla

import chisel3._
import chisel3.experimental._
import chisel3.util._



//this module is to active dat and wt

class NV_NVDLA_CMAC_CORE_mac(implicit val conf: cmacConfiguration) extends RawModule {

    val io = IO(new Bundle {
        //general clock
        val nvdla_core_clk = Input(Clock())
        val nvdla_wg_clk = Input(Clock())       
        val nvdla_core_rstn = Input(Bool())

        //config
        val cfg_is_wg = Input(Bool())
        val cfg_reg_en = Input(Bool())

        //input
        val dat_actv_data = Input(Vec(conf.CMAC_ATOMC, conf.CMAC_TYPE(conf.CMAC_BPE.W)))
        val dat_actv_nz = Input(Vec(conf.CMAC_ATOMC, Bool()))
        val dat_actv_pvld = Input(Vec(conf.CMAC_ATOMC, Bool()))

        val wt_actv_data = Input(Vec(conf.CMAC_ATOMC, conf.CMAC_TYPE(conf.CMAC_BPE.W)))
        val wt_actv_nz = Input(Vec(conf.CMAC_ATOMC, Bool()))
        val wt_actv_pvld = Input(Vec(conf.CMAC_ATOMC, Bool()))

        //output
        val mac_out_data = Output(conf.CMAC_TYPE(conf.CMAC_RESULT_WIDTH.W))
        val mac_out_pvld = Output(Bool())         
    })




//     
//          ┌─┐       ┌─┐
//       ┌──┘ ┴───────┘ ┴──┐
//       │                 │
//       │       ───       │
//       │  ─┬┘       └┬─  │
//       │                 │
//       │       ─┴─       │
//       │                 │
//       └───┐         ┌───┘
//           │         │
//           │         │
//           │         │
//           │         └──────────────┐
//           │                        │
//           │                        ├─┐
//           │                        ┌─┘    
//           │                        │
//           └─┐  ┐  ┌───────┬──┐  ┌──┘         
//             │ ─┤ ─┤       │ ─┤ ─┤         
//             └──┴──┘       └──┴──┘ 
                

    withClockAndReset(io.nvdla_core_clk, !io.nvdla_core_rstn){

    val op_out_pvld = VecInit(Seq.fill(conf.CMAC_ATOMC)(false.B))
    val mout = VecInit(Seq.fill(conf.CMAC_ATOMC)(conf.CMAC_TYPE(0)))
 
    for(i <- 0 to conf.CMAC_ATOMC-1){
        op_out_pvld(i) := io.wt_actv_pvld(i)&io.dat_actv_pvld(i)&io.wt_actv_nz(i)&io.dat_actv_nz(i)
        when(op_out_pvld(i)){
            mout(i) := io.wt_actv_data(i)*io.dat_actv_data(i)
        }
        .otherwise{
            mout(i) := conf.CMAC_TYPE(0, conf.CMAC_RESULT_WIDTH)
        }
    }  

    val sum_out = mout.reduce(_+&_)
    
    //add retiming
    val pp_pvld_d0 = io.dat_actv_pvld(0) & io.wt_actv_pvld(0)

    io.mac_out_data := ShiftRegister(sum_out, conf.CMAC_OUT_RETIMING, pp_pvld_d0)
    io.mac_out_pvld := ShiftRegister(pp_pvld_d0, conf.CMAC_OUT_RETIMING, pp_pvld_d0)


    }
  }
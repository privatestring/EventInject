package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "simulated_tpsl_close_futures")
class SimulatedFuturesTpslCloseActivity : Activity() {

    @Boom(index = 0, key = "key_tpsl_close_order_entry", desc = "止盈止损平仓SimulatedFuturesTpslOrderEntry的json结构")
    var tpslOrderEntryString: String? = null

}
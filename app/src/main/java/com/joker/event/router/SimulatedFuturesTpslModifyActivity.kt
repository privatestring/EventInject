package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "simulated_tpsl_modify_futures")
class SimulatedFuturesTpslModifyActivity : Activity() {

    @Boom(index = 0, key = "key_tpsl_modify_order_entry", desc = "止盈止损改单FuturesTpslOrderEntry的json结构")
    var tpslOrderEntryString: String? = null

}
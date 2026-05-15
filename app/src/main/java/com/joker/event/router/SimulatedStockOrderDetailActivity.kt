package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "simulated_stock_order_detail")
class SimulatedStockOrderDetailActivity : Activity() {

    @Boom(index = 0, key = "com.joker.event.router.orderDetailEntryJsonIntentKey", desc = "下单信息OrderDetailEntry的json结构")
    var orderDetailEntryJson: String? = null

}
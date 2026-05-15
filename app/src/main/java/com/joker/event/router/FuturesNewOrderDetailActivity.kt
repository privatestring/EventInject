package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "futures_order_detail")
class FuturesNewOrderDetailActivity : Activity() {

    @Boom(index = 0, key = "com.joker.event.router.placeOrderEntryJsonIntentKey", desc = "下单信息PlaceOrderEntry的json结构")
    var placeOrderEntryJson: String? = null

}
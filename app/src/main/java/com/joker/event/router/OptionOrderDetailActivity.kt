package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "option_order_detail")
class OptionOrderDetailActivity : Activity() {

    @Boom(index = 0, key = "com.joker.event.router.placeOrderEntryJsonIntentKey", desc = "下单信息PlaceOrderEntry的json结构")
    var placeOrderEntryJson: String? = null

}
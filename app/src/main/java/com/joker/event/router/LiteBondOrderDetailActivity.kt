package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "lite_bond_order_detail")
class LiteBondOrderDetailActivity : Activity() {

    @Boom(index = 0, key = "com.joker.event.router.placeOrderEntryJsonIntentKey", desc = "详情页面信息OrderDetailEntry的json结构")
    var placeOrderEntryJson: String? = null

}
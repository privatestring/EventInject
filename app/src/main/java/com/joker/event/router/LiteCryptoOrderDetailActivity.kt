package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "lite_crypto_order_detail")
class LiteCryptoOrderDetailActivity : Activity() {

    @Boom(index = 0, key = "com.joker.event.router.orderDetailEntryJsonIntentKey", desc = "详情页面信息OrderDetailEntry的json结构")
    var orderDetailEntryJson: String? = null

}
package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "cryptoFeeDetailPage")
class CryptoFeeDetailActivity : Activity() {

    @Boom(index = 0, key = "accountKey", desc = "数字货币费用详情")
    var accountKey: String? = null

    @Boom(index = 1, key = "orderId", desc = "记录id")
    var orderId: String? = null

}
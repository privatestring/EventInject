package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "cryptoHistoryPage")
class CryptoHistoryActivity : Activity() {

    @Boom(index = 0, key = "accountKey", desc = "数字货币历史记录页面，提供给伪协议跳转")
    var accountKey: String? = null

    @Boom(index = 1, key = "type", desc = "数字货币历史记录页面，指定查看类型，默认位订单")
    var type: String? = null

}
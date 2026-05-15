package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "vegaAnalystChat")
class VegaAnalystActivity : Activity() {

    @Boom(index = 0, key = "tickerId", desc = "标的tickerId")
    var tickerId: String? = null

    @Boom(index = 1, key = "symbol", desc = "标的symbol")
    var symbol: String? = null

    @Boom(index = 2, key = "preferences", isOptional = true, desc = "偏好设置")
    var preferences: String? = null

}
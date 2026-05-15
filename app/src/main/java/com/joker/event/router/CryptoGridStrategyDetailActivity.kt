package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "crypto_grid_strategy_detail")
class CryptoGridStrategyDetailActivity : Activity() {

    @Boom(index = 0, key = "accountKey", desc = "账户信息")
    var accountKey: String? = null

    @Boom(index = 1, key = "strategyId", desc = "策略信息")
    var strategyId: String? = null

}
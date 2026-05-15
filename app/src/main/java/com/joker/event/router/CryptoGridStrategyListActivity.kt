package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "crypto_grid_strategy_list")
class CryptoGridStrategyListActivity : Activity() {

    @Boom(index = 0, key = "accountKey", desc = "账户信息")
    var accountKey: String? = null

    @Boom(index = 1, key = "tickerId", desc = "标的信息")
    var tickerId: String? = null

    @Boom(index = 2, key = "selectedStatus", desc = "策略状态")
    var selectedStatus: String? = null

    @Boom(index = 3, key = "disSymbol", desc = "标的信息")
    var disSymbol: String? = null

}
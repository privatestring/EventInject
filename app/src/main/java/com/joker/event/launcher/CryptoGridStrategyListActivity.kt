package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import launcher.Boom

class CryptoGridStrategyListActivity : Activity() {
    @Boom(index = 0, key = "accountKey")
    var accountKey: String? = null

    @Boom(index = 1, key = "tickerId")
    var tickerId: String? = null

    @Boom(index = 2, key = "selectedStatus")
    var selectedStatus: String? = null

    @Boom(index = 3, key = "disSymbol")
    var disSymbol: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CryptoGridStrategyListActivityLauncher.bind(this)
    }
}

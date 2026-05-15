package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import com.webull.core.framework.bean.TickerBase
import launcher.Boom

class UsFundPlaceOrderActivity : Activity() {
    @Boom(index = 0)
    var brokerId: Int = 0

    @Boom(index = 1)
    var tickerBase: TickerBase? = null

    @Boom(index = 2)
    var isBuy: Boolean = true

    @Boom(index = 3)
    var isModify: Boolean = true

    @Boom(index = 4, isOptional = true)
    var modifyOrderId: String? = null

    @Boom(index = 5, isOptional = true)
    var modifyQuantity: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UsFundPlaceOrderActivityLauncher.bind(this)
    }
}

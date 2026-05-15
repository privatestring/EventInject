package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import com.webull.commonmodule.networkinterface.tradeapi.beans.HKIPOOrderDetail
import com.webull.core.framework.bean.TickerBase
import launcher.Boom

class HkPlaceIPOOrderActivity : Activity() {
    @Boom(index = 1, key = "account_key")
    var accountKey: String = ""

    @Boom(index = 2)
    var tickerInfo: TickerBase = TickerBase()

    @Boom(index = 3, isOptional = true)
    var mOrderInfo: HKIPOOrderDetail? = null

    @Boom(index = 4, isOptional = true)
    var mEnableMultiAccount: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HkPlaceIPOOrderActivityLauncher.bind(this)
    }
}

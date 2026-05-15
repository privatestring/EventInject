package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import com.webull.library.tradenetwork.bean.AccountInfo
import com.webull.wefolio.pojo.TradeWefolioTickerInfo
import java.util.ArrayList
import launcher.Boom

class WefolioTradeSuccessActivity : Activity() {
    @Boom(index = 0)
    var accountInfo: AccountInfo? = null

    @Boom(index = 1)
    var wefolioOrderId: String? = null

    @Boom(index = 2)
    var sourceWefolioId: String? = null

    @Boom(index = 3)
    var wefolioTickerList: ArrayList<TradeWefolioTickerInfo>? = null

    @Boom(index = 4, isOptional = true)
    var wealthTabId: String? = null

    @Boom(index = 5, isOptional = true)
    var wealthBizId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WefolioTradeSuccessActivityLauncher.bind(this)
    }
}

package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import com.webull.commonmodule.trade.bean.AUPortfolioRecurring
import com.webull.core.framework.bean.TickerBase
import com.webull.library.broker.common.home.page.fragment.orders.recurring.bean.RecurringDetailInfo
import com.webull.order.recurring.crypto.place.data.RegularInvestmentPlanBean
import com.webull.wefolio.pojo.TradeWefolioInfo
import launcher.Boom
import launcher.MakeResult

@MakeResult(includeStartForResult = true)
class RecurringPlaceOrderActivity : Activity() {
    @Boom(index = 1, isOptional = true)
    var mTickerInfo: TickerBase? = null
    @Boom(index = 2)
    var accountKey: String? = null
    @Boom(index = 3, isOptional = true)
    var mRecurringDetailInfo: RecurringDetailInfo? = null
    @Boom(index = 4, isOptional = true)
    var mTradeWefolioInfo: TradeWefolioInfo? = null
    @Boom(index = 5, isOptional = true)
    var isDynamic: Boolean? = null
    @Boom(index = 6, isOptional = true)
    var portfolioRecurring: AUPortfolioRecurring? = null
    @Boom(index = 7, isOptional = true)
    var cryptoDetail: RegularInvestmentPlanBean? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RecurringPlaceOrderActivityLauncher.bind(this)
    }
}

package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.commonmodule.trade.bean.AUPortfolioRecurring
import com.webull.core.framework.bean.TickerBase
import com.webull.library.broker.common.home.page.fragment.orders.recurring.bean.RecurringDetailInfo
import launcher.Boom

class AURecurringPlaceOrderFragment : Fragment() {
    @Boom(index = 1)
    var tickerInfo: TickerBase? = null

    @Boom(index = 2)
    var mBrokerId: Int? = null

    @Boom(index = 3, isOptional = true)
    var recurringDetailInfo: RecurringDetailInfo? = null

    @Boom(index = 4, isOptional = true)
    var portfolioRecurring: AUPortfolioRecurring? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        AURecurringPlaceOrderFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

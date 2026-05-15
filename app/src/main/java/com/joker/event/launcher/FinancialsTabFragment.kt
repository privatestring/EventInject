package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.commonmodule.bean.TickerKey
import com.webull.ticker.subtab.item.TickerSubTabType
import com.webull.ticker.tab.item.TickerTabType
import launcher.Boom

class FinancialsTabFragment : Fragment() {
    @Boom(index = 0, key = "mTickerKey")
    var tickerKey: TickerKey = TickerKey()

    @Boom(index = 1, isOptional = true, key = "TICKER_TAB_TYPE")
    var tabType: TickerTabType = TickerTabType()

    @Boom(index = 2, isOptional = true, key = "ticker_company_tab_type")
    var subTabType: TickerSubTabType? = null

    @Boom(index = 3, isOptional = true)
    var isPage: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        FinancialsTabFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

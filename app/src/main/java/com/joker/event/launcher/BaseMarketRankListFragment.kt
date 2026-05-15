package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.commonmodule.networkinterface.securitiesapi.beans.MarketHomeCard
import com.webull.commonmodule.networkinterface.securitiesapi.beans.market.MarketCommonTabBean
import com.webull.newmarket.beans.MarketCellConfig
import java.util.ArrayList
import launcher.Boom

class BaseMarketRankListFragment : Fragment() {
    @Boom(index = 0, key = "regionId")
    var regionId: String? = null

    @Boom(index = 1, key = "source")
    var cardData: MarketHomeCard? = null

    @Boom(index = 2)
    var tabData: MarketCommonTabBean? = null

    @Boom(index = 3)
    var leftMarketHeadCellList: ArrayList<MarketCellConfig>? = null

    @Boom(index = 4)
    var rightMarketHeadCellList: ArrayList<MarketCellConfig>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        BaseMarketRankListFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

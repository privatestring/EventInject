package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.commonmodule.networkinterface.securitiesapi.beans.MarketHomeCard
import launcher.Boom

class MarketStarItemDetailFragment : Fragment() {
    @Boom(index = 0, key = "regionId")
    var regionId: String? = null

    @Boom(index = 1, key = "source")
    var cardData: MarketHomeCard? = null

    @Boom(index = 2, key = "tabId")
    var defaultTabId: String? = null

    @Boom(index = 3, key = "rankType")
    var rankType: String? = null

    @Boom(index = 4, isOptional = true, key = "name")
    var pageTraceNode: String? = "marketStars"

    @Boom(index = 5, isOptional = true)
    var isAUETFsMarketStar: Boolean? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MarketStarItemDetailFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.commonmodule.networkinterface.securitiesapi.beans.MarketHomeCard
import launcher.Boom

class BaseMarketMomentumIndicatorDetailFragment : Fragment() {
    @Boom(index = 0, key = "regionId")
    var regionId: String? = null

    @Boom(index = 1, key = "cardData")
    var cardData: MarketHomeCard? = null

    @Boom(index = 2, key = "groupId", isOptional = true)
    var tabId: String? = null

    @Boom(index = 3, isOptional = true)
    var techId: Int? = null

    @Boom(index = 4, isOptional = true)
    var containerName: String? = null

    @Boom(index = 5, key = "tabId", isOptional = true)
    var defaultTabId: String? = null

    @Boom(index = 6, isOptional = true)
    var filterClose: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        BaseMarketMomentumIndicatorDetailFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

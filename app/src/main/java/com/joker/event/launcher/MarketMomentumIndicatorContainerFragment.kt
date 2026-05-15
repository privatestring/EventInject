package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.commonmodule.networkinterface.securitiesapi.beans.MarketHomeCard
import launcher.Boom

class MarketMomentumIndicatorContainerFragment : Fragment() {
    @Boom(index = 0, key = "regionId")
    var regionId: String? = null

    @Boom(index = 1, key = "cardData")
    var cardData: MarketHomeCard? = null

    @Boom(index = 2, key = "title")
    var pageTitle: String? = null

    @Boom(index = 3, isOptional = true, key = "groupId")
    var groupId: String? = null

    @Boom(index = 4, key = "containerName")
    var containerName: String? = null

    @Boom(index = 5, isOptional = true, key = "tab")
    var defaultTab: String? = null

    @Boom(index = 6, isOptional = true, key = "groupType")
    var groupType: String? = null

    @Boom(index = 7, isOptional = true, key = "tabId")
    var defaultTabId: String? = null

    @Boom(index = 8, isOptional = true, key = "filterClose")
    var filterClose: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MarketMomentumIndicatorContainerFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

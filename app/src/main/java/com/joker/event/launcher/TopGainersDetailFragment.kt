package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.commonmodule.networkinterface.securitiesapi.beans.MarketHomeCard
import launcher.Boom

class TopGainersDetailFragment : Fragment() {
    @Boom(index = 0, key = "regionId")
    var regionId: String? = null

    @Boom(index = 1, key = "cardData")
    var cardData: MarketHomeCard? = null

    @Boom(index = 2, isOptional = true, key = "groupId")
    var defaultGroupId: String = ""

    @Boom(index = 3, isOptional = true, key = "groupType")
    var defaultGroupType: String? = null

    @Boom(index = 4, isOptional = true, key = "tabId")
    var defaultTabId: String? = null

    @Boom(index = 5, isOptional = true, key = "rankType")
    var rankType: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TopGainersDetailFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

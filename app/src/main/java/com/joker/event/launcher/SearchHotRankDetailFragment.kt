package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.commonmodule.networkinterface.securitiesapi.beans.MarketHomeCard
import launcher.Boom

class SearchHotRankDetailFragment : Fragment() {
    @Boom(index = 0, key = "regionId")
    var regionId: String? = null

    @Boom(index = 1, key = "source")
    var cardData: MarketHomeCard? = null

    @Boom(index = 2, key = "tabId")
    var defaultTabId: String? = null

    @Boom(index = 3, key = "rankType")
    var rankType: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        SearchHotRankDetailFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

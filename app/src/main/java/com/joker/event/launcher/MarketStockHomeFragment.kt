package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class MarketStockHomeFragment : Fragment() {
    @Boom(index = 0, key = "regionCode")
    var regionCode: String = "2005"

    @Boom(index = 1, key = "regionId")
    var region: String = "6"

    @Boom(index = 2, isOptional = true, key = "isShowActionBar")
    var isShowActionBar: String = "false"

    @Boom(index = 3, isOptional = true)
    var title: String = ""

    @Boom(index = 4, isOptional = true)
    var isSupportSearch: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MarketStockHomeFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

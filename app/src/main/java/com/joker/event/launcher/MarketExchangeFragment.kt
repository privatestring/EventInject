package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class MarketExchangeFragment : Fragment() {
    @Boom(index = 0, key = "groupId")
    var groupId: String = ""

    @Boom(index = 1, key = "key_exchange_detail_title")
    var name: String = ""

    @Boom(index = 2, key = "regionId")
    var regionId: String = ""

    @Boom(index = 3, key = "tabId")
    var rankType: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MarketExchangeFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

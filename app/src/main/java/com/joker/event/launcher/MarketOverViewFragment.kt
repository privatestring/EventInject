package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class MarketOverViewFragment : Fragment() {
    @Boom(index = 0, key = "regionId")
    var region: String = "6"

    @Boom(index = 1, key = "index")
    var index: String = "6"

    @Boom(index = 2, key = "netflow")
    var netFlow: String = "6"

    @Boom(index = 3, isOptional = true, key = "scroll_to_card")
    var scrollToCardName: String = "6"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MarketOverViewFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class PortfolioTopListHomeFragment : Fragment() {
    @Boom(index = 0)
    var pageIndex: String? = "0"

    @Boom(index = 1, isOptional = true, key = "tab")
    var defaultTab: String? = null

    @Boom(index = 2, isOptional = true, key = "regionId")
    var regionId: String? = null

    @Boom(index = 3, isOptional = true, key = "tabId")
    var defaultTabId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        PortfolioTopListHomeFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

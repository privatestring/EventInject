package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "future_hot_rank")
class FutureHotTabRankListFragment : Fragment() {

    @Boom(index = 0, key = "regionId", desc = "区域ID")
    var regionId: String = ""

    @Boom(index = 1, key = "tabId", desc = "当前选中的Tab ID")
    var currentTabId: String? = null

}
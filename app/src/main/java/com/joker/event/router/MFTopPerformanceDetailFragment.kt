package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "mf_top_performance")
class MFTopPerformanceDetailFragment : Fragment() {

    @Boom(index = 1, key = "regionId", desc = "当前regionId")
    var regionId: String? = null

    @Boom(index = 2, key = "tabId", isOptional = true, desc = "当前TabId")
    var currentTabId: String? = null

    @Boom(index = 3, key = "title", isOptional = true, desc = "标题")
    var title: String? = null

}
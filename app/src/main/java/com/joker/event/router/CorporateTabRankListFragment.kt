package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "corporate_tab_rank")
class CorporateTabRankListFragment : Fragment() {

    @Boom(index = 0, key = "regionId", isOptional = true, desc = "区域ID")
    var regionId: String = ""

    @Boom(index = 1, key = "tabId", isOptional = true, desc = "当前选中的Tab ID")
    var currentTabId: String? = null

}
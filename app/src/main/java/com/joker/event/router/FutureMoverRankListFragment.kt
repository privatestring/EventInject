package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "future_mover")
class FutureMoverRankListFragment : Fragment() {

    @Boom(index = 0, key = "regionId", desc = "区域ID")
    var regionId: String = ""

    @Boom(index = 1, key = "groupId", desc = "区域ID")
    var groupId: String = ""

    @Boom(index = 2, key = "tabId", isOptional = true, desc = "当前选中的Tab ID")
    var currentTabId: String? = null

    @Boom(index = 3, key = "tabData", isOptional = true, desc = "Tab列表")
    var tabDataStr: String? = null

}
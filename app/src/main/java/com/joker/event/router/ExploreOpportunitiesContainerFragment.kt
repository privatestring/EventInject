package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "portfolio_toplist")
class ExploreOpportunitiesContainerFragment : Fragment() {

    @Boom(index = 0, key = "tab", desc = "Pro 设置选中的tab")
    var defaultTab: String? = null

    @Boom(index = 1, key = "regionId", desc = "区域")
    var regionId: String? = null

    @Boom(index = 2, key = "tabId", desc = "默认显示的tabId")
    var defaultTabId: String? = null

}
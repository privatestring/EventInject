package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "hotNews")
class HotSpotNewsRankContainerFragment : Fragment() {

    @Boom(index = 0, key = "hotNewsColumns", desc = "新闻集 列表")
    var tabListStr: String? = null

    @Boom(index = 1, key = "hotNewsDefaultIndex", desc = "默认选中的新闻集")
    var defaultTabId: String? = null

    @Boom(index = 2, key = "regionId", desc = "当前打开的地区id")
    var regionId: String? = null

}
package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "news_aggregation_page")
class AggregateNewsHomeFragment : Fragment() {

    @Boom(index = 0, key = "regionId", desc = "当前打开的地区id")
    var regionId: String? = null

    @Boom(index = 1, key = "newsTabId", isOptional = true, desc = "默认选中的新闻集")
    var defaultTabId: String? = null

    @Boom(index = 2, key = "isMainTabNews", isOptional = true, desc = "是否是MainTab的新闻模块")
    var isMainTabNews: String? = null

}
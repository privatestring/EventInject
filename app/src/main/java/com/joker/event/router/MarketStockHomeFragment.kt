package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "market_stock_index")
class MarketStockHomeFragment : Fragment() {

    @Boom(index = 0, key = "regionCode", desc = "市场tab，请求的regionCode，用来区分是市场的哪个子tab")
    var regionCode: String = ""

    @Boom(index = 1, key = "regionId", desc = "市场tab，请求的region")
    var region: String = ""

    @Boom(index = 2, key = "isShowActionBar", isOptional = true, desc = "市场tab，是否显示ActionBar")
    var isShowActionBar: String = ""

    @Boom(index = 3, isOptional = true, desc = "市场tab，显示的标题")
    var title: String = ""

    @Boom(index = 4, isOptional = true, desc = "市场tab，支持显示ActionBar前提下是否支持全局搜索")
    var isSupportSearch: String = ""

}
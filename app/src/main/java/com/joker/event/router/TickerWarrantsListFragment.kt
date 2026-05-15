package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "ticker_warrant_activity")
class TickerWarrantsListFragment : Fragment() {

    @Boom(index = 0, key = "regionId", desc = "地区id,不同地区的窝轮不一样")
    var regionId: String = ""

    @Boom(index = 1, key = "tickerId", desc = "是否绑定某个标的")
    var tickerId: String? = null

    @Boom(index = 2, key = "isDLC", desc = "sg DLC 类型")
    var isDLC: String? = null

}
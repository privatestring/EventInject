package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "future_day_margin")
class FutureDayMarginListFragment : Fragment() {

    @Boom(index = 0, key = "regionId", desc = "RegionId")
    var regionId: String? = null

    @Boom(index = 1, key = "title", isOptional = true, desc = "标题")
    var title: String? = null

}
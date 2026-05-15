package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "option_opportunities")
class OptionOpportunitiesDetailsMainFragment : Fragment() {

    @Boom(index = 0, key = "regionId", desc = "区域ID")
    var regionId: String = ""

    @Boom(index = 1, desc = "市场收藏组id")
    var groupId: String? = null

    @Boom(index = 2, desc = "市场tabs里面的子id")
    var rankType: String? = null

}
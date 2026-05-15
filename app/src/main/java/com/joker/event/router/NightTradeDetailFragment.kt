package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "nightTrade")
class NightTradeDetailFragment : Fragment() {

    @Boom(index = 0, key = "regionId", desc = "区域ID")
    var regionId: String = ""

}
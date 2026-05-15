package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "lite_ipo_center")
class LiteIpoBuyingListFragment : Fragment() {

    @Boom(index = 0, key = "regionId", isOptional = true, desc = "区域ID")
    var regionId: String = ""

}
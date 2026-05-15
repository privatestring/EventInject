package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "futures_main_activity_v3")
class FutureHomeFragment : Fragment() {

    @Boom(index = 0, key = "regionId", isOptional = true, desc = "regionId，不传默认为 REGION_US ")
    var regionId: String = ""

    @Boom(index = 1, isOptional = true, desc = "是否是独立落地页")
    var isStandalone: String = ""

}
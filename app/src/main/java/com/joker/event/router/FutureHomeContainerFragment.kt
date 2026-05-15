package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "futures_main_activity")
class FutureHomeContainerFragment : Fragment() {

    @Boom(index = 0, key = "region_id", desc = "地区id")
    var region: String = ""

}
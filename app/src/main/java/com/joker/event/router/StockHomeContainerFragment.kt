package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "stockLandPage")
class StockHomeContainerFragment : Fragment() {

    @Boom(index = 0, key = "regionCode", desc = "市场tab，请求的regionCode，用来区分是市场的哪个子tab")
    var regionCode: String = ""

    @Boom(index = 1, key = "regionId", isOptional = true, desc = "市场tab，请求的region")
    var region: String? = null

}
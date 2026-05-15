package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "optionLandPage")
class OptionsHomeFragment : Fragment() {

    @Boom(index = 0, isOptional = true, desc = "regionId，不传默认为 REGION_US ")
    var regionId: String = ""

    @Boom(index = 1, isOptional = true, desc = "是否是独立落地页")
    var isStandalone: String = ""

}
package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "litemarketmovers")
class LiteMoversContainerFragment : Fragment() {

    @Boom(index = 0, key = "groupType", isOptional = true, desc = "需要选中的子榜单")
    var defaultTab: String? = null

    @Boom(index = 1, key = "regionId", isOptional = true, desc = "区域")
    var regionId: String? = null

}
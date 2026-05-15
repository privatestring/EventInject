package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "savingsIndex")
class SavingsIndexFragment : Fragment() {

    @Boom(index = 0, key = "regionId", desc = "区域ID")
    var regionId: String? = null

    @Boom(index = 1, key = "securitySubType", isOptional = true, desc = "储蓄产品类型")
    var securitySubType: String? = null

}
package com.joker.event.launcher

import com.webull.commonmodule.jump.action.ParamConsts
import launcher.Boom

class RanksTemplateIntentParams {
    @Boom(index = 0, key = "rankId")
    var rankId: String? = null

    @Boom(index = 1, key = "regionId")
    var regionId: String? = null

    @Boom(index = 2, key = "brokerId", isOptional = true)
    var brokerId: String? = null

    @Boom(index = 3, key = "rankType")
    var rankType: String? = null

    @Boom(index = 4, key = "page_type")
    var pageType: String? = null

    @Boom(index = 5, key = "tickerId")
    var tickerId: String? = null
}

package com.joker.event.launcher

import com.webull.commonmodule.jump.action.ParamConsts
import com.webull.rankstemplate.pojo.RanksData
import launcher.Boom

class SingleRanksTemplateIntentParams {
    @Boom(index = 1, key = "cardData", isOptional = true)
    var cardData: RanksData? = null
}

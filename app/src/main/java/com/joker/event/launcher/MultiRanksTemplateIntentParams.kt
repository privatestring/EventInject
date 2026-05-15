package com.joker.event.launcher

import com.webull.commonmodule.jump.action.ParamConsts
import com.webull.rankstemplate.pojo.RanksData
import launcher.Boom

class MultiRanksTemplateIntentParams {
    @Boom(index = 0, key = "cardData", isOptional = true)
    var cardDataList: ArrayList<RanksData>? = null

    @Boom(index = 1, key = "groupId", isOptional = true)
    var defaultGroupId: String = ""
}

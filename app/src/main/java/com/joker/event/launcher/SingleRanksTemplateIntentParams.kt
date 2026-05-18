package com.joker.event.launcher

import com.webull.rankstemplate.pojo.RanksData
import launcher.Boom
import launcher.IncludeParentBoom

@IncludeParentBoom
class SingleRanksTemplateIntentParams : RanksTemplateIntentParams() {
    @Boom(index = 6, key = "cardData", isOptional = true)
    var cardData: RanksData? = null
}


@IncludeParentBoom
class SingleRanksTemplatexxx : RanksTemplateIntentParams() {
    
}

package com.joker.event.launcher

import com.webull.commonmodule.jump.action.ParamConsts
import com.webull.rankstemplate.bean.RanksCellConfig
import com.webull.rankstemplate.pojo.RanksData
import com.webull.rankstemplate.pojo.RanksTabData
import launcher.Boom

class RanksListTemplateIntentParams {
    @Boom(index = 0, key = ParamConsts.CommonParam.BUNDLE_KEY_SOURCE)
    var rankData: RanksData? = null

    @Boom(index = 1)
    var tabData: RanksTabData? = null

    @Boom(index = 2)
    var leftRanksHeadCellList: ArrayList<RanksCellConfig>? = null

    @Boom(index = 3)
    var rightRanksHeadCellList: ArrayList<RanksCellConfig>? = null
}

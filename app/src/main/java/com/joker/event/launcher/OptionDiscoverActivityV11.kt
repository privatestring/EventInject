package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import com.webull.commonmodule.bean.TickerKey
import launcher.Boom

class OptionDiscoverActivityV11 : Activity() {
    @Boom(index = 1, key = "discoverGoal")
    var mGoalType: String? = null

    @Boom(index = 2, key = "ticker")
    var mTickerKey: TickerKey? = null

    @Boom(index = 3, isOptional = true, key = "step")
    var mStep: Int = -1

    @Boom(index = 4, isOptional = true, key = "strategy")
    var mStrategy: String? = null

    @Boom(index = 5, isOptional = true, key = "strategy_title")
    var mStrategyTitle: String? = null

    @Boom(index = 6, isOptional = true, key = "is_from_lite_more")
    var isFromMore: Boolean? = null

    @Boom(index = 7, isOptional = true, key = "is_from_lite_stay_below")
    var isFromLiteStayBelow: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OptionDiscoverActivityV11Launcher.bind(this)
    }
}

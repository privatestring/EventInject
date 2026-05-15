package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import com.webull.core.framework.bean.TickerBase
import com.webull.library.broker.common.position.PositionType
import launcher.Boom

class LitePositionDetailActivity : Activity() {
    @Boom(index = 1)
    var accountKey: String = ""

    @Boom(index = 2)
    var positionId: String = ""

    @Boom(index = 3)
    var positionType: PositionType = PositionType()

    @Boom(index = 4, isOptional = true)
    var ticker: TickerBase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LitePositionDetailActivityLauncher.bind(this)
    }
}

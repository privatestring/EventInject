package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import com.webull.library.broker.common.position.PositionType
import launcher.Boom

class InstSimulatedPositionDetailsActivity : Activity() {
    @Boom(index = 1)
    var accountKey: String = ""

    @Boom(index = 2)
    var positionId: String = ""

    @Boom(index = 3)
    var positionType: PositionType = PositionType()

    @Boom(index = 4, isOptional = true)
    var tickerId: String? = null

    @Boom(index = 5, isOptional = true)
    var fromStrategy: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InstSimulatedPositionDetailsActivityLauncher.bind(this)
    }
}

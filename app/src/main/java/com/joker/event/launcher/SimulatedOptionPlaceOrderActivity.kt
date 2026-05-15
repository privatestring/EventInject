package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import launcher.Boom

class SimulatedOptionPlaceOrderActivity : Activity() {
    @Boom(index = 0, key = "paper_id")
    var paperId: String? = null

    @Boom(index = 1, key = "ticker_id")
    var stockId: String = ""

    @Boom(index = 2, key = "option_Strategy")
    var strategy: String = ""

    @Boom(index = 3, key = "option_leg_list")
    var optionLegsJsonStr: String = ""

    @Boom(index = 4, isOptional = true, key = "order_action")
    var action: String? = ""

    @Boom(index = 5, isOptional = true, key = "order_quantity")
    var quantity: String? = "1"

    @Boom(index = 6, isOptional = true, key = "order_is_modify")
    var isModify: String? = ""

    @Boom(index = 7, isOptional = true, key = "order_is_close_position")
    var isClosePosition: String? = ""

    @Boom(index = 2222, isOptional = true, key = "order_json_str")
    var modifyOrderStr: String? = null

    @Boom(index = 3333, isOptional = true, key = "close_position_json_str")
    var closePositionStr: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SimulatedOptionPlaceOrderActivityLauncher.bind(this)
    }
}

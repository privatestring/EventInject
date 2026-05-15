package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "stock_ai_trade")
class AiVoicePlaceOrderActivity : Activity() {

    @Boom(index = 0, desc = "AiVoicePlaceOrderEntry入参对象json字符串")
    var aiVoicePlaceOrderEntryJson: String? = null

}
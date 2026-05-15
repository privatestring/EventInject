package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "ticker_detail")
class TickerActivityV3 : Activity() {

    @Boom(index = 0, key = "tickerEntryJson", desc = "个股信息TickerEntry的json结构")
    var tickerEntryJson: String? = null

}
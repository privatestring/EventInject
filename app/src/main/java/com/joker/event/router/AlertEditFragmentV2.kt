package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "alert.edit")
class AlertEditFragmentV2 : Fragment() {

    @Boom(index = 0, key = "ticker_KEY", desc = "TickerKey Json 字符串")
    var ticker: String = ""

}
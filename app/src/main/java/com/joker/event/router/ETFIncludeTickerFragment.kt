package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "target_fragment_etf_in_ticker")
class ETFIncludeTickerFragment : Fragment() {

    @Boom(index = 0, key = "tickerId", useFieldKey = true, desc = "ticker的id，网络请求时需要用到")
    var tickerId: String = ""

    @Boom(index = 1, key = "symbolName", useFieldKey = true, desc = "ticker的symbol，展示数据时需要用到")
    var symbolName: String = ""

}
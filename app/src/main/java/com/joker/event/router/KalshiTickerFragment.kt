package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "kalshi_quotes")
class KalshiTickerFragment : Fragment() {

    @Boom(index = 0, key = "params", desc = "kalshi个股页面的数据类型json对象KalshiTickerParam")
    var kalshiTickerJson: String = ""

}
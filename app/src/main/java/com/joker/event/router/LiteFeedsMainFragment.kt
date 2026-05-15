package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "liteFeedsMainFragment")
class LiteFeedsMainFragment : Fragment() {

    @Boom(index = 0, key = "com.joker.event.router.tickerKeyJsonIntentKey", desc = "Ticker 数据")
    var tickerKeyJson: String? = null

}
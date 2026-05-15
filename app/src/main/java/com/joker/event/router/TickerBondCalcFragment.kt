package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "tickerBondCalc")
class TickerBondCalcFragment : Fragment() {

    @Boom(index = 0, key = "com.joker.event.router.tickerIdIntentKey", desc = "债券tickerId")
    var tickerId: String = ""

    @Boom(index = 1, key = "com.joker.event.router.isOddLotSupportIntentKey", desc = "是否是碎债")
    var isOddLotSupport: String = ""

}
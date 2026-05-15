package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "page_crypto_strategy_center")
class CryptoStrategyCenterFragment : Fragment() {

    @Boom(index = 0, key = "com.joker.event.router.tickerIdIntentKey", desc = "tickerId")
    var tickerId: String = ""

    @Boom(index = 1, key = "com.joker.event.router.disSymbolIntentKey", desc = "disSymbol")
    var disSymbol: String = ""

}
package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "vega_conversion_page")
class AiTickerVegaAnalystFragment : Fragment() {

    @Boom(index = 0, key = "key_ticker_id", desc = "股票 ID")
    var tickerId: String = ""

    @Boom(index = 1, key = "key_ticker_dis_symbol", desc = "股票代码")
    var symbol: String = ""

    @Boom(index = 2, key = "source", isOptional = true, desc = "来源")
    var source: String? = null

    @Boom(index = 3, key = "custom_text", isOptional = true, desc = "自定义文本")
    var customText: String? = null

    @Boom(index = 4, key = "history_config", isOptional = true, desc = "自定义文本")
    var configHistory: String? = null

}
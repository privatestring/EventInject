package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "tickerBidAsk")
class TickerLiteBidAskFragment : Fragment() {

    @Boom(index = 0, key = "tickerEntryJson", desc = "个股信息TickerEntry的json结构")
    var tickerEntryJson: String? = null

    @Boom(index = 1, key = "tickerDataLevel", isOptional = true, desc = "是否提前知道当前的lv级别")
    var dataLevel: String = ""

    @Boom(index = 2, key = "clickFinishContainer", isOptional = true, desc = "是否点击后仅退出当前页面")
    var clickFinishContainer: String? = null

}
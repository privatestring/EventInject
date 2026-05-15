package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "ticker_chart")
class TickerFullChartActivity : Activity() {

    @Boom(index = 0, key = "tickerEntryJson", desc = "个股信息TickerEntry的json结构")
    var tickerEntryJson: String? = null

    @Boom(index = 1, key = "full_chart_params", desc = "全屏图表的页面传参信息")
    var chartParamsJson: String? = null

}
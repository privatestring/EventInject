package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "voice_quote_setting")
class VoiceQuoteSettingActivity : Activity() {

    @Boom(index = 0, key = "key_ticker_key", desc = "个股信息TickerKey的json结构")
    var tickerKeyJson: String? = null

    @Boom(index = 1, key = "isStopWatch", desc = "是否停止监控")
    var isStopWatch: String? = null

}
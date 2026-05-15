package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "lite_chart_setting")
class LiteChartSettingFragment : Fragment() {

    @Boom(index = 0, key = "com.joker.event.router.regionIdIntentKey", desc = "regionId")
    var regionId: String = ""

    @Boom(index = 1, key = "com.joker.event.router.isCryptoIntentKey", desc = "是否是数字货币")
    var isCrypto: String = ""

}
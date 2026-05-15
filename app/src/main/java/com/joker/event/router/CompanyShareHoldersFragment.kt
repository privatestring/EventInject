package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "Shareholders")
class CompanyShareHoldersFragment : Fragment() {

    @Boom(index = 0, key = "tickerKey", desc = "档案信息")
    var tickerKeyJson: String = ""

    @Boom(index = 1, key = "initTab", desc = "初始化选中tab")
    var initTab: String? = null

    @Boom(index = 2, key = "customTitle", isOptional = true, desc = "自定义标题")
    var customTitle: String? = null

}
package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "futuresTypePage")
class IndexFuturesFragment : Fragment() {

    @Boom(index = 0, key = "region_id", desc = "地区")
    var region: String = ""

    @Boom(index = 1, key = "broker_id", desc = "账户类型")
    var brokerId: String = ""

    @Boom(index = 2, key = "rank_type", desc = "期货请求要的type,如 micro ")
    var rankType: String? = null

    @Boom(index = 3, key = "title", desc = "期货名称，在落地返回的")
    var title: String? = null

    @Boom(index = 4, key = "tab_id", isOptional = true, desc = "初始化选中的tab，如：micro.MYM")
    var selectedTabId: String? = null

}
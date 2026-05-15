package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "option_occmemo_list")
class OptionsOccMemoDetailContainerFragment : Fragment() {

    @Boom(index = 0, key = "regionId", isOptional = true, desc = "regionId")
    var regionId: String = ""

    @Boom(index = 1, isOptional = true, desc = "默认选中的榜单Id")
    var rankType: String? = null

    @Boom(index = 2, isOptional = true, desc = "页面标题")
    var title: String? = null

}
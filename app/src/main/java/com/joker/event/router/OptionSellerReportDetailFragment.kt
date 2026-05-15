package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "OptionSeller")
class OptionSellerReportDetailFragment : Fragment() {

    @Boom(index = 0, key = "regionId", desc = "区域Id")
    var regionId: String? = null

}
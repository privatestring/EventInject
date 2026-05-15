package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "FinancialReportOutlookLandPage")
class FinancialReportOutlookIndexFragment : Fragment() {

    @Boom(index = 0, key = "regionId", desc = "region Id")
    var region: String = ""

}
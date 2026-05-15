package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "option_earnings_signals")
class OptionEarningsSignalsDetailsMainFragment : Fragment() {

    @Boom(index = 0, key = "regionId", desc = "区域ID")
    var regionId: String = ""

}
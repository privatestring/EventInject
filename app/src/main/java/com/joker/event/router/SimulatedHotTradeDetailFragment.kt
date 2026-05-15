package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "simulate_trade_hot_rank_detail")
class SimulatedHotTradeDetailFragment : Fragment() {

    @Boom(index = 0, key = "period_type", desc = "周期时段")
    var periodType: String? = null

}
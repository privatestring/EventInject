package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "portfolioMultiChartFragment")
class PortfolioMultiChartFragment : Fragment() {

    @Boom(index = 0, key = "com.joker.event.router.curPortfolioIdIntentKey", desc = "当前选择组合ID")
    var curPortfolioId: String = ""

}